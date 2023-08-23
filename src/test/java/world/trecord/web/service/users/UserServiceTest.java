package world.trecord.web.service.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
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
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.service.users.response.UserCommentsResponse;
import world.trecord.web.service.users.response.UserInfoResponse;
import world.trecord.web.service.users.response.UserRecordLikeListResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class UserServiceTest {

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
        UserEntity newUser = userService.createNewUserWith(email);

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
        UserInfoResponse response = userService.getUserInfoBy(saveUser.getId());

        //then
        Assertions.assertThat(response.getNickname()).isEqualTo(nickname);
        Assertions.assertThat(response.getIntroduction()).isEqualTo(introduction);
        Assertions.assertThat(response.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 조회하면 예외가 발생한다")
    void findUserByNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when // then
        Assertions.assertThatThrownBy(() -> userService.getUserInfoBy(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("사용자가 작성한 댓글을 등록 시간 내림차순으로 조회하여 반환한다")
    void getUserCommentsByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity1 = recordRepository.save(createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        RecordEntity recordEntity2 = recordRepository.save(createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String content1 = "content1";
        String content2 = "content2";
        String content3 = "content3";
        String content4 = "content4";

        CommentEntity commentEntity1 = commentRepository.save(createCommentEntity(userEntity, recordEntity1, content1));
        CommentEntity commentEntity2 = commentRepository.save(createCommentEntity(userEntity, recordEntity2, content2));
        CommentEntity commentEntity3 = commentRepository.save(createCommentEntity(userEntity, recordEntity2, content3));
        CommentEntity commentEntity4 = commentRepository.save(createCommentEntity(userEntity, recordEntity1, content4));

        //when
        UserCommentsResponse response = userService.getUserCommentsBy(userEntity.getId());

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
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        //when
        UserCommentsResponse response = userService.getUserCommentsBy(userEntity.getId());

        //then
        Assertions.assertThat(response.getComments()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 조회하여 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place1", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place3", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity4 = createRecordEntity(feedEntity, "record4", "place4", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = createUserRecordLikeEntity(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = createUserRecordLikeEntity(userEntity, recordEntity4);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when
        UserRecordLikeListResponse response = userService.getUserRecordLikeListBy(userEntity.getId());

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
    @DisplayName("사용자가 좋아요한 기록이 없으면 UserRecordLikeListResponse의 records 필드를 빈 배열로 반환한다")
    void getUserRecordLikeListWithEmptyListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        //when
        UserRecordLikeListResponse response = userService.getUserRecordLikeListBy(userEntity.getId());

        //then
        Assertions.assertThat(response.getRecords()).isEmpty();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, String title, String place, LocalDateTime date, String content, String weather, String satisfaction, String feeling) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(title)
                .place(place)
                .date(date)
                .content(content)
                .weather(weather)
                .transportation(satisfaction)
                .feeling(feeling)
                .build();
    }

    private CommentEntity createCommentEntity(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private UserRecordLikeEntity createUserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }
}