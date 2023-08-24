package world.trecord.web.service.record;

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
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordDeleteRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;
import world.trecord.web.service.record.response.RecordCreateResponse;
import world.trecord.web.service.record.response.RecordDeleteResponse;
import world.trecord.web.service.record.response.RecordInfoResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.web.exception.CustomExceptionError.*;

@IntegrationTestSupport
class RecordServiceTest {

    @Autowired
    RecordService recordService;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Test
    @DisplayName("기록 작성자가 기록을 조회하면 기록 상세 정보, 댓글 리스트를 반환한다")
    void getRecordInfoByRecordWriterIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity commenter2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordInfoResponse recordInfoResponse = recordService.getRecordInfo(recordEntity.getId(), writer.getId());

        //then
        Assertions.assertThat(recordInfoResponse.getWriterId()).isEqualTo(writer.getId());
        Assertions.assertThat(recordInfoResponse.getTitle()).isEqualTo(recordEntity.getTitle());
        Assertions.assertThat(recordInfoResponse.getContent()).isEqualTo(recordEntity.getContent());
        Assertions.assertThat(recordInfoResponse.getIsUpdatable()).isTrue();
        Assertions.assertThat(recordInfoResponse.getComments())
                .hasSize(2)
                .extracting("commentId", "commenterId", "isUpdatable", "content")
                .containsExactly(
                        tuple(commentEntity1.getId(), commentEntity1.getUserEntity().getId(), false, commentEntity1.getContent()),
                        tuple(commentEntity2.getId(), commentEntity2.getUserEntity().getId(), false, commentEntity2.getContent())
                );
    }

    @Test
    @DisplayName("댓글 작성자가 기록을 조회하면 기록 상세 정보, 댓글 리스트를 반환한다")
    void getRecordInfoByCommenterIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity commenter2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordInfoResponse recordInfoResponse = recordService.getRecordInfo(recordEntity.getId(), commenter1.getId());

        //then
        Assertions.assertThat(recordInfoResponse.getWriterId()).isEqualTo(writer.getId());
        Assertions.assertThat(recordInfoResponse.getTitle()).isEqualTo(recordEntity.getTitle());
        Assertions.assertThat(recordInfoResponse.getContent()).isEqualTo(recordEntity.getContent());
        Assertions.assertThat(recordInfoResponse.getIsUpdatable()).isFalse();
        Assertions.assertThat(recordInfoResponse.getComments())
                .hasSize(2)
                .extracting("commentId", "commenterId", "isUpdatable", "content")
                .containsExactly(
                        tuple(commentEntity1.getId(), commentEntity1.getUserEntity().getId(), true, commentEntity1.getContent()),
                        tuple(commentEntity2.getId(), commentEntity2.getUserEntity().getId(), false, commentEntity2.getContent())
                );
    }

    @Test
    @DisplayName("익명 사용자가 기록을 조회하면 기록 상세 정보, 댓글 리스트를 반환한다")
    void getRecordInfoByAnonymoudUserTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity commenter2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordInfoResponse recordInfoResponse = recordService.getRecordInfo(recordEntity.getId(), null);

        //then
        Assertions.assertThat(recordInfoResponse.getWriterId()).isEqualTo(writer.getId());
        Assertions.assertThat(recordInfoResponse.getTitle()).isEqualTo(recordEntity.getTitle());
        Assertions.assertThat(recordInfoResponse.getContent()).isEqualTo(recordEntity.getContent());
        Assertions.assertThat(recordInfoResponse.getIsUpdatable()).isFalse();
        Assertions.assertThat(recordInfoResponse.getComments())
                .hasSize(2)
                .extracting("commentId", "commenterId", "isUpdatable", "content")
                .containsExactly(
                        tuple(commentEntity1.getId(), commentEntity1.getUserEntity().getId(), false, commentEntity1.getContent()),
                        tuple(commentEntity2.getId(), commentEntity2.getUserEntity().getId(), false, commentEntity2.getContent())
                );
    }

    @Test
    @DisplayName("존재하지 않는 기록 아이디로 조회하면 예외가 발생한다")
    void getRecordInfoByNotExistingRecordIdTest() throws Exception {
        //when //then
        Assertions.assertThatThrownBy(() -> recordService.getRecordInfo(0L, 0L))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_RECORD);
    }

    @Test
    @DisplayName("피드 작성자가 기록을 생성한다")
    void createRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        String title = "title";
        String place = "jeju";
        String feeling = "feeling";
        String weather = "weather";
        String satisfaction = "best";
        String content = "content";
        String companion = "companion";

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title(title)
                .date(LocalDateTime.of(2021, 10, 1, 0, 0))
                .place(place)
                .feeling(feeling)
                .weather(weather)
                .transportation(satisfaction)
                .content(content)
                .companion(companion)
                .build();

        //when
        RecordCreateResponse response = recordService.createRecord(writer.getId(), request);

        //then
        Assertions.assertThat(response.getRecordId()).isNotNull();
        Assertions.assertThat(response.getWriterId()).isEqualTo(writer.getId());
        Assertions.assertThat(response.getFeedId()).isEqualTo(feedEntity.getId());
        Assertions.assertThat(response)
                .extracting("date", "place", "feeling", "weather", "transportation", "content", "companion")
                .containsExactly(LocalDate.of(2021, 10, 1), place, feeling, weather, satisfaction, content, companion);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 기록을 생성하려고 하면 예외가 발생한다")
    void createRecordWithNotExistingUserIdTest() throws Exception {
        //given
        RecordCreateRequest request = RecordCreateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.createRecord(0L, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 기록을 생성하려고 하면 예외가 발생한다")
    void createRecordWithNotExistingFeedIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(0L)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.createRecord(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 기록을 생성하려고 하면 예외가 발생한다")
    void createRecordWithNotWriterIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity other = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.createRecord(other.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("피드 작성자가 본인의 기록을 수정 요청하면 수정된 기록 정보를 반환한다")
    void updateRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        String changedTitle = "change title";
        LocalDateTime changedDate = LocalDateTime.of(2021, 10, 2, 0, 0);
        String changedPlace = "changed place";
        String changedContent = "changed content";
        String changedFeeling = "changed feeling";
        String changedWeather = "changed weather";
        String changedCompanion = "changed changedCompanion";
        String changedTransportation = "changed transportation";

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .feedId(feedEntity.getId())
                .recordId(recordEntity.getId())
                .title(changedTitle)
                .date(changedDate)
                .place(changedPlace)
                .content(changedContent)
                .feeling(changedFeeling)
                .weather(changedWeather)
                .companion(changedCompanion)
                .transportation(changedTransportation)
                .build();

        //when
        RecordInfoResponse response = recordService.updateRecord(writer.getId(), request);

        //then
        RecordEntity changedRecord = recordRepository.findById(recordEntity.getId()).get();

        Assertions.assertThat(changedRecord)
                .extracting("title", "date", "place", "content", "feeling", "weather", "companion", "transportation")
                .containsExactly(changedTitle, changedDate, changedPlace, changedContent, changedFeeling, changedWeather, changedCompanion, changedTransportation);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 기록을 수정하려고 하면 예외가 발생한다")
    void updateRecordWithNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;
        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.updateRecord(notExistingUserId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 기록을 수정하려고 하면 예외가 발생한다")
    void updateRecordWithNotExistingFeedIdTest() throws Exception {
        //given
        Long notExistingFeedId = 0L;
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .feedId(notExistingFeedId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.updateRecord(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 기록을 수정하려고 하면 예외가 발생한다")
    void updateRecordWithNotFeedWriterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity other = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .feedId(feedEntity.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.updateRecord(other.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 기록 아이디로 기록을 수정하려고 하면 예외가 발생한다")
    void updateRecordWithNotExistingRecordIdTest() throws Exception {
        //given
        Long notExistingRecordId = 0L;

        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity other = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .feedId(feedEntity.getId())
                .recordId(notExistingRecordId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.updateRecord(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_RECORD);
    }

    @Test
    @DisplayName("피드 작성자가 기록을 삭제하면 댓글 리스트와 함께 삭제된다")
    void deleteRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity commenter1 = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        UserEntity commenter2 = userRepository.save(UserEntity.builder()
                .email("test2@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        CommentEntity commentEntity1 = createCommentEntity(commenter1, recordEntity, "content1");
        CommentEntity commentEntity2 = createCommentEntity(commenter2, recordEntity, "content2");

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        RecordDeleteRequest request = RecordDeleteRequest.builder()
                .feedId(feedEntity.getId())
                .recordId(recordEntity.getId())
                .build();

        //when
        RecordDeleteResponse response = recordService.deleteRecord(writer.getId(), request);

        //then
        Assertions.assertThat(recordRepository.findById(recordEntity.getId())).isEmpty();
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않은 사용자가 기록을 삭제하려고 하면 예외가 발생한다")
    void deleteRecordWithNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;
        RecordDeleteRequest request = RecordDeleteRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.deleteRecord(notExistingUserId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 기록을 삭제하려고 하면 예외가 발생한다")
    void deleteRecordWithNotExistingFeedIdTest() throws Exception {
        //given
        Long notExistingFeedId = 0L;

        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        RecordDeleteRequest request = RecordDeleteRequest.builder()
                .feedId(notExistingFeedId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.deleteRecord(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("존재하지 않는 레코드 아이디로 기록을 삭제하려고 하면 예외가 발생한다")
    void deleteRecordWithNotExistingRecordIdTest() throws Exception {
        //given
        Long notExistingRecordId = 0L;

        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordDeleteRequest request = RecordDeleteRequest.builder()
                .feedId(feedEntity.getId())
                .recordId(notExistingRecordId)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.deleteRecord(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NOT_EXISTING_RECORD);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 기록을 삭제하려고 하면 예외가 발생한다")
    void deleteRecordWithNotFeedWriterTest() throws Exception {
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity viewer = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        RecordDeleteRequest request = RecordDeleteRequest.builder()
                .feedId(feedEntity.getId())
                .recordId(recordEntity.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.deleteRecord(viewer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("기록에 대해 좋아요 한 사용자가 기록을 조회하면 RecordInfoResponse의 필드 liked = true다")
    void getRecordInfoByTestByUserWhoLikedOnRecord() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity viewer = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        userRecordLikeRepository.save(createUserRecordLikeEntity(viewer, recordEntity));

        //when
        RecordInfoResponse response = recordService.getRecordInfo(recordEntity.getId(), viewer.getId());

        //then
        Assertions.assertThat(response.getLiked()).isTrue();
    }

    @Test
    @DisplayName("기록에 대해 좋아요 하지 않은 사용자가 기록을 조회하면 RecordInfoResponse의 필드 liked = false다")
    void getRecordInfoByTestByUserWhoNotLikedOnRecord() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        UserEntity viewer = userRepository.save(UserEntity.builder()
                .email("test1@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        RecordInfoResponse response = recordService.getRecordInfo(recordEntity.getId(), viewer.getId());

        //then
        Assertions.assertThat(response.getLiked()).isFalse();
    }

    @Test
    @DisplayName("인증받지 않은 사용자가 기록을 조회하면 RecordInfoResponse의 필드 liked = false다")
    void getRecordInfoByTestByUserWhoNotAuthenticatedOnRecord() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(writer, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2021, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        RecordInfoResponse response = recordService.getRecordInfo(recordEntity.getId(), null);

        //then
        Assertions.assertThat(response.getLiked()).isFalse();
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
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

    private UserRecordLikeEntity createUserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }
}