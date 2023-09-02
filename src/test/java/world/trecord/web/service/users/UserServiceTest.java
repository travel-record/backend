package world.trecord.web.service.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.service.users.request.UserUpdateRequest;
import world.trecord.web.service.users.response.UserCommentsResponse;
import world.trecord.web.service.users.response.UserInfoResponse;
import world.trecord.web.service.users.response.UserRecordLikeListResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.web.exception.CustomExceptionError.NICKNAME_DUPLICATED;

@IntegrationTestSupport
class UserServiceTest extends ContainerBaseTest {

    @Autowired
    UserService userService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Test
    @DisplayName("이메일로 새로운 사용자를 생성할 수 있다")
    void createUserWithEmailTest() throws Exception {
        //given
        String email = "test@test.com";

        //when
        UserEntity newUser = userService.createNewUser(email);

        //then
        assertThat(newUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자 정보를 조회할 수 있다")
    void findUserByUserIdTest() throws Exception {
        //given
        String email = "test@email.com";
        String nickname = "nickname";
        String imageUrl = "http://localhost/pictures";
        String introduction = "hello";

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        UserEntity saveUser = userRepository.save(userEntity);

        //when
        UserInfoResponse response = userService.getUser(saveUser.getId());

        //then
        Assertions.assertThat(response)
                .extracting("nickname", "introduction", "imageUrl")
                .containsExactly(nickname, introduction, imageUrl);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 조회하면 예외가 발생한다")
    void findUserByNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when // then
        Assertions.assertThatThrownBy(() -> userService.getUser(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자가 작성한 댓글을 등록 시간 내림차순으로 조회하여 반환한다")
    void getUserCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity));

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        String content4 = "content4";

        CommentEntity commentEntity1 = createComment(userEntity, recordEntity1, content1);
        CommentEntity commentEntity2 = createComment(userEntity, recordEntity2, content2);
        CommentEntity commentEntity3 = createComment(userEntity, recordEntity2, content3);
        CommentEntity commentEntity4 = createComment(userEntity, recordEntity1, content4);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when
        UserCommentsResponse response = userService.getUserComments(userEntity.getId());

        //then
        Assertions.assertThat(response.getComments())
                .hasSize(4)
                .extracting("recordId", "commentId", "content")
                .containsExactly(
                        tuple(recordEntity1.getId(), commentEntity4.getId(), commentEntity4.getContent()),
                        tuple(recordEntity2.getId(), commentEntity3.getId(), commentEntity3.getContent()),
                        tuple(recordEntity2.getId(), commentEntity2.getId(), commentEntity2.getContent()),
                        tuple(recordEntity1.getId(), commentEntity1.getId(), commentEntity1.getContent())
                );
    }

    @Test
    @DisplayName("사용자가 등록한 댓글이 없으면 빈 배열을 반환한다")
    void getUserEmptyCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        UserCommentsResponse response = userService.getUserComments(userEntity.getId());

        //then
        Assertions.assertThat(response.getComments()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 조회하여 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);
        RecordEntity recordEntity3 = createRecord(feedEntity);
        RecordEntity recordEntity4 = createRecord(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(userEntity, recordEntity4);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when
        UserRecordLikeListResponse response = userService.getUserRecordLikeList(userEntity.getId());

        //then
        Assertions.assertThat(response.getRecords())
                .hasSize(2)
                .extracting("recordId", "title", "authorNickname", "imageUrl")
                .containsExactly(
                        tuple(recordEntity4.getId(), recordEntity4.getTitle(), userEntity.getNickname(), recordEntity4.getImageUrl()),
                        tuple(recordEntity1.getId(), recordEntity1.getTitle(), userEntity.getNickname(), recordEntity1.getImageUrl())
                );
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트에서 soft delete한 좋아요 리스트를 제외한 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByWhenUserLikedCancelTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity));

        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        UserRecordLikeEntity userRecordLikeEntity1 = createRecordLike(other, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createRecordLike(other, recordEntity2);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        userRecordLikeRepository.softDeleteById(userRecordLikeEntity2.getId());

        //when
        UserRecordLikeListResponse response = userService.getUserRecordLikeList(other.getId());

        //then
        Assertions.assertThat(response.getRecords())
                .hasSize(1)
                .extracting("recordId")
                .containsOnly(recordEntity1.getId());
    }

    @Test
    @DisplayName("새로운 닉네임으로 업데이트 한다")
    void updateUserTest() throws Exception {
        //given
        userRepository.save(createUser("test@email.com"));

        UserEntity userEntity = userRepository.save(createUser("test1@email.com"));

        String changedNickname = "changed nickname";
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname(changedNickname)
                .build();

        //when
        UserInfoResponse response = userService.updateUser(userEntity.getId(), updateRequest);

        //then
        Assertions.assertThat(userRepository.findById(userEntity.getId()).get())
                .extracting("nickname")
                .isEqualTo(changedNickname);
    }

    @Test
    @DisplayName("이미 저장된 닉네임으로 업데이트 요청하면 예외가 발생한다")
    void updateUserWhenDuplicatedNicknameTest() throws Exception {
        //given
        String savedNickname = "nickname";

        userRepository.save(UserEntity.builder()
                .nickname(savedNickname)
                .email("test@email.com")
                .build());

        UserEntity userEntity = userRepository.save(createUser("test1@email.com"));

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname(savedNickname)
                .build();

        //when
        Assertions.assertThatThrownBy(() -> userService.updateUser(userEntity.getId(), updateRequest))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NICKNAME_DUPLICATED);
    }

    @Test
    @DisplayName("새로운 소개글로 업데이트한다")
    void updateUserWhenNewDescTest() throws Exception {
        //given
        userRepository.save(createUser("test@email.com"));

        String originalNickname = "nickname";
        String changedIntroduction = "change introduction";

        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .nickname(originalNickname)
                .introduction("before introduction")
                .email("test1@email.com")
                .build());

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname(originalNickname)
                .introduction(changedIntroduction)
                .build();

        //when
        UserInfoResponse response = userService.updateUser(userEntity.getId(), updateRequest);

        //then
        Assertions.assertThat(userRepository.findById(userEntity.getId()).get())
                .extracting("nickname", "introduction")
                .containsExactly(originalNickname, changedIntroduction);
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록이 없으면 UserRecordLikeListResponse의 records 필드를 빈 배열로 반환한다")
    void getUserRecordLikeListWithEmptyListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        UserRecordLikeListResponse response = userService.getUserRecordLikeList(userEntity.getId());

        //then
        Assertions.assertThat(response.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("사용자를 조회하여 UserContext로 반환한다")
    void loadUserContextByUserIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        //when
        UserContext userContext = userService.getUserContextOrException(userEntity.getId());

        //then
        Assertions.assertThat(userContext.getId()).isEqualTo(userEntity.getId());
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 UsernameNotFoundException 예외가 발생한다")
    void loadUserContextByUserIdWhenUserNotFoundTest() throws Exception {
        //given
        long notExistingUserId = -1L;

        //when //then
        Assertions.assertThatThrownBy(() -> userService.getUserContextOrException(notExistingUserId))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2021, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2021, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("title")
                .place("place")
                .date(LocalDateTime.of(2022, 3, 2, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

    private UserRecordLikeEntity createRecordLike(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }
}