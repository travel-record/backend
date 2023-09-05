package world.trecord.service.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.record.request.RecordCreateRequest;
import world.trecord.service.record.request.RecordSequenceSwapRequest;
import world.trecord.service.record.request.RecordUpdateRequest;
import world.trecord.service.record.response.RecordCommentsResponse;
import world.trecord.service.record.response.RecordCreateResponse;
import world.trecord.service.record.response.RecordInfoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
@IntegrationTestSupport
class RecordServiceTestAbstract extends AbstractContainerBaseTest {

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
    RecordSequenceRepository recordSequenceRepository;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Test
    @DisplayName("기록 작성자가 기록을 조회하면 기록 상세 정보, 댓글 리스트를 반환한다")
    void getRecordInfoByRecordWriterIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        RecordInfoResponse recordInfoResponse = recordService.getRecord(Optional.of(writer.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(recordInfoResponse.getWriterId()).isEqualTo(writer.getId());
        Assertions.assertThat(recordInfoResponse.getTitle()).isEqualTo(recordEntity.getTitle());
        Assertions.assertThat(recordInfoResponse.getContent()).isEqualTo(recordEntity.getContent());
        Assertions.assertThat(recordInfoResponse.getIsUpdatable()).isTrue();
    }

    @Test
    @DisplayName("댓글 작성자가 기록을 조회하면 기록 상세 정보, 댓글 리스트를 반환한다")
    void getRecordInfoByCommenterIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity commenter1 = userRepository.save(createUser("test1@email.com"));
        UserEntity commenter2 = userRepository.save(createUser("test2@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity);
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordInfoResponse recordInfoResponse = recordService.getRecord(Optional.of(commenter1.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(recordInfoResponse.getWriterId()).isEqualTo(writer.getId());
        Assertions.assertThat(recordInfoResponse.getTitle()).isEqualTo(recordEntity.getTitle());
        Assertions.assertThat(recordInfoResponse.getContent()).isEqualTo(recordEntity.getContent());
        Assertions.assertThat(recordInfoResponse.getIsUpdatable()).isFalse();
    }

    @Test
    @DisplayName("익명 사용자가 기록을 조회하면 기록 상세 정보, 댓글 리스트를 반환한다")
    void getRecordInfoByAnonymoudUserTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity commenter1 = userRepository.save(createUser("test1@email.com"));
        UserEntity commenter2 = userRepository.save(createUser("test2@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        CommentEntity commentEntity1 = createComment(commenter1, recordEntity);
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity);

        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when
        RecordInfoResponse response = recordService.getRecord(Optional.empty(), recordEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("writerId", "title", "content", "isUpdatable")
                .containsExactly(writer.getId(), recordEntity.getTitle(), recordEntity.getContent(), false);
    }

    @Test
    @DisplayName("존재하지 않는 기록 아이디로 조회하면 예외가 발생한다")
    void getRecordInfoByNotExistingRecordIdTest() throws Exception {
        //given
        long viewerId = 1L;
        long notExistingRecordId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.getRecord(Optional.of(viewerId), notExistingRecordId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 작성자가 기록을 생성한다")
    void createRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));

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
        Assertions.assertThat(recordRepository.findById(response.getRecordId())).isPresent();
    }

    @Test
    @DisplayName("기록을 생성할 때 같은 날짜에 작성된 기록이 있으면 마지막 순서 번호 + 1 번호를 가진다")
    void createRecordSequenceNumberTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        int sequence = 1;
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, sequence));
        recordSequenceRepository.insertOrIncrement(feedEntity.getId(), recordEntity.getDate());

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title("title")
                .date(recordEntity.getDate())
                .place("jeju")
                .feeling("feeling")
                .weather("weather")
                .transportation("best")
                .content("content")
                .companion("companion")
                .build();

        //when
        recordService.createRecord(writer.getId(), request);

        //then
        List<RecordEntity> all = recordRepository.findAll();
        Assertions.assertThat(all)
                .hasSize(2)
                .extracting("sequence")
                .containsExactly(sequence, sequence + 1);
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 기록을 생성하려고 하면 예외가 발생한다")
    void createRecordWithNotExistingFeedIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(0L)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.createRecord(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 기록을 생성하려고 하면 예외가 발생한다")
    void createRecordWithNotWriterIdTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test1@email.com"));
        UserEntity other = userRepository.save(createUser("test2@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));

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
        UserEntity writer = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        String changedTitle = "change title";
        LocalDateTime changedDate = LocalDateTime.of(2021, 10, 2, 0, 0);
        String changedPlace = "changed place";
        String changedContent = "changed content";
        String changedFeeling = "changed feeling";
        String changedWeather = "changed weather";
        String changedCompanion = "changed changedCompanion";
        String changedTransportation = "changed transportation";

        RecordUpdateRequest request = RecordUpdateRequest.builder()
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
        recordService.updateRecord(writer.getId(), recordEntity.getId(), request);

        //then
        Assertions.assertThat(recordRepository.findById(recordEntity.getId()))
                .isPresent()
                .hasValueSatisfying(record -> {
                    Assertions.assertThat(record)
                            .extracting("title", "date", "place", "content", "feeling", "weather", "companion", "transportation")
                            .containsExactly(changedTitle, changedDate, changedPlace, changedContent, changedFeeling, changedWeather, changedCompanion, changedTransportation);
                });
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 기록을 수정하려고 하면 예외가 발생한다")
    void updateRecordWithNotFeedWriterTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));
        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.updateRecord(other.getId(), recordEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("존재하지 않는 기록 아이디로 기록을 수정하려고 하면 예외가 발생한다")
    void updateRecordWithNotExistingRecordIdTest() throws Exception {
        //given
        Long notExistingRecordId = 0L;
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.updateRecord(writer.getId(), notExistingRecordId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 작성자가 기록을 soft delete 한다")
    void deleteRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        recordService.deleteRecord(writer.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 기록 아이디로 기록을 삭제하려고 하면 예외가 발생한다")
    void deleteRecordWithNotExistingFeedIdTest() throws Exception {
        //given
        Long notExistingRecordId = 0L;
        UserEntity writer = userRepository.save(createUser("test@email.com"));

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.deleteRecord(writer.getId(), notExistingRecordId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(RECORD_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 기록을 삭제하려고 하면 예외가 발생한다")
    void deleteRecordWithNotFeedWriterTest() throws Exception {
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity viewer = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.deleteRecord(viewer.getId(), recordEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("기록에 대해 좋아요 한 사용자가 기록을 조회하면 RecordInfoResponse의 필드 liked = true다")
    void getRecordInfoByTestByUserWhoLikedOnRecord() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity viewer = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        userRecordLikeRepository.save(createRecordLike(viewer, recordEntity));

        //when
        RecordInfoResponse response = recordService.getRecord(Optional.of(viewer.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(response.getLiked()).isTrue();
    }

    @Test
    @DisplayName("기록에 대해 좋아요 하지 않은 사용자가 기록을 조회하면 RecordInfoResponse의 필드 liked = false다")
    void getRecordInfoByTestByUserWhoNotLikedOnRecord() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity viewer = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        RecordInfoResponse response = recordService.getRecord(Optional.of(viewer.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(response.getLiked()).isFalse();
    }

    @Test
    @DisplayName("인증받지 않은 사용자가 기록을 조회하면 RecordInfoResponse의 필드 liked = false다")
    void getRecordInfoByTestByUserWhoNotAuthenticatedOnRecord() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        RecordInfoResponse response = recordService.getRecord(Optional.empty(), recordEntity.getId());

        //then
        Assertions.assertThat(response.getLiked()).isFalse();
    }

    @Test
    @DisplayName("기록에 등록된 댓글들을 등록 시간 오름차 순으로 조회하여 RecordCommentsResponse로 반환한다")
    void getRecordCommentsTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity viewer = userRepository.save(createUser("viewer@email.com"));
        UserEntity commenter1 = userRepository.save(createUser("test1@email.com"));
        UserEntity commenter2 = userRepository.save(createUser("test2@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));
        CommentEntity commentEntity1 = createComment(commenter1, recordEntity);
        CommentEntity commentEntity2 = createComment(commenter2, recordEntity);

        commentRepository.saveAll(List.of(commentEntity2, commentEntity1));

        //when
        RecordCommentsResponse response = recordService.getRecordComments(Optional.of(viewer.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(response.getComments())
                .hasSize(2)
                .extracting("commentId", "commenterId", "commenterImageUrl", "isUpdatable", "content")
                .containsExactly(
                        tuple(commentEntity2.getId(), commenter2.getId(), commenter2.getImageUrl(), false, commentEntity2.getContent()),
                        tuple(commentEntity1.getId(), commenter1.getId(), commenter1.getImageUrl(), false, commentEntity1.getContent())
                );
    }

    @Test
    @DisplayName("댓글 작성자가 soft delete한 댓글은 댓글 리스트에 포함되지 않는다")
    void getRecordCommentsWhenCommentSoftDeletedTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity commenter = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));
        CommentEntity commentEntity1 = createComment(commenter, recordEntity);
        CommentEntity commentEntity2 = createComment(commenter, recordEntity);
        CommentEntity commentEntity3 = createComment(commenter, recordEntity);

        commentRepository.saveAll(List.of(commentEntity2, commentEntity1, commentEntity3));

        commentRepository.softDeleteById(commentEntity2.getId());

        //when
        RecordCommentsResponse response = recordService.getRecordComments(Optional.of(commenter.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(response.getComments())
                .hasSize(2)
                .extracting("commentId")
                .containsExactly(commentEntity1.getId(), commentEntity3.getId());
    }

    @Test
    @DisplayName("기록에 등록된 댓글들이 없을때 댓글 리스트가 빈 배열인 RecordCommentsResponse로 반환한다")
    void getRecordCommentsReturnsCommentsEmptyTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity, 0));

        //when
        RecordCommentsResponse response = recordService.getRecordComments(Optional.of(writer.getId()), recordEntity.getId());

        //then
        Assertions.assertThat(response.getComments()).isEmpty();
    }

    @Test
    @DisplayName("같은 피드를 가지는 두 개의 기록 순서를 스왑한다")
    void updateRecordSequenceTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        int recordEntitySeq1 = 1;
        int recordEntitySeq2 = 2;
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity, recordEntitySeq1));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity, recordEntitySeq2));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when
        recordService.swapRecordSequence(writer.getId(), request);

        //then
        Assertions.assertThat(recordRepository.findById(recordEntity1.getId()))
                .isPresent()
                .hasValueSatisfying(recordEntity -> {
                    Assertions.assertThat(recordEntity.getSequence()).isEqualTo(recordEntitySeq2);
                });

        Assertions.assertThat(recordRepository.findById(recordEntity2.getId()))
                .isPresent()
                .hasValueSatisfying(recordEntity -> {
                    Assertions.assertThat(recordEntity.getSequence()).isEqualTo(recordEntitySeq1);
                });
    }

    @Test
    @DisplayName("다른 피드를 가지는 기록들을 스왑 요청하면 INVALID_ARGUMENT 예외가 발생한다")
    void updateRecordSequenceWhenNotSameFeedTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity1 = feedRepository.save(createFeed(writer));
        FeedEntity feedEntity2 = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity1, 0));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity2, 0));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.swapRecordSequence(writer.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(INVALID_ARGUMENT);
    }

    @Test
    @DisplayName("피드 수정 권한이 없는 사용자가 기록 순서 스왑 요청하면 FORBIDDEN 예외가 발생한다")
    void updateRecordSequenceWhenUserForbiddenTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(writer));
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity, 0));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity, 0));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordService.swapRecordSequence(other.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
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

    private RecordEntity createRecord(FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 10, 1, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }

    private CommentEntity createComment(UserEntity userEntity, RecordEntity recordEntity) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content("content")
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