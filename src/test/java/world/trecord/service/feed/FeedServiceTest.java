package world.trecord.service.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.feed.request.FeedCreateRequest;
import world.trecord.dto.feed.request.FeedUpdateRequest;
import world.trecord.dto.feed.response.FeedCreateResponse;
import world.trecord.dto.feed.response.FeedInfoResponse;
import world.trecord.dto.feed.response.FeedListResponse;
import world.trecord.dto.feed.response.FeedRecordsResponse;
import world.trecord.exception.CustomException;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static world.trecord.exception.CustomExceptionError.FEED_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.FORBIDDEN;

@Transactional
@IntegrationTestSupport
class FeedServiceTest extends AbstractContainerBaseTest {

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    FeedService feedService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @DisplayName("사용자가 등록한 여행 시작 시간 내림차순으로 정렬된 피드 리스트를 반환한다")
    void getFeedListByUserId() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        FeedEntity feedEntity4 = createFeed(savedUserEntity, LocalDateTime.of(2021, 12, 21, 0, 0), LocalDateTime.of(2021, 12, 25, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        //when
        FeedListResponse feedListResponse = feedService.getFeedList(savedUserEntity.getId());

        //then
        Assertions.assertThat(feedListResponse.getFeeds()).extracting("name", "startAt")
                .containsExactly(
                        tuple("name", LocalDate.of(2021, 12, 21)),
                        tuple("name", LocalDate.of(2021, 12, 10)),
                        tuple("name", LocalDate.of(2021, 10, 4)),
                        tuple("name", LocalDate.of(2021, 9, 30))
                );
    }

    @Test
    @DisplayName("사용자 등록한 피드가 없다면 빈 배열을 반환한다")
    void getEmptyFeedListByUserId() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        //when
        FeedListResponse feedListResponse = feedService.getFeedList(savedUserEntity.getId());

        //then
        Assertions.assertThat(feedListResponse.getFeeds()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 등록한 특정 피드를 기록과 함께 반환한다")
    void getFeedByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);
        RecordEntity recordEntity3 = createRecord(feedEntity);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        FeedInfoResponse response = feedService.getFeed(Optional.of(userEntity.getId()), feedEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("writerId", "feedId", "startAt", "endAt")
                .containsExactly(userEntity.getId(), feedEntity.getId(),
                        feedEntity.convertStartAtToLocalDate(), feedEntity.convertEndAtToLocalDate());
        Assertions.assertThat(response.getRecords().stream().map(FeedInfoResponse.Record::getTitle)).containsExactly("title", "title", "title");
    }

    @Test
    @DisplayName("사용자가 soft delete한 피드는 반환하지 않는다")
    void getFeedByFeedIdWhenFeedSoftDeletedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));

        feedRepository.delete(feedEntity3);

        //when
        FeedListResponse feedListResponse = feedService.getFeedList(savedUserEntity.getId());

        //then
        Assertions.assertThat(feedListResponse.getFeeds())
                .hasSize(2)
                .extracting("name")
                .containsExactly(feedEntity1.getName(), feedEntity2.getName());
    }

    @Test
    @DisplayName("사용자가 soft delete한 기록은 반환하지 않는다")
    void getFeedByWhenRecordSoftDeletedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 4, 0, 0)));

        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);
        RecordEntity recordEntity3 = createRecord(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        recordRepository.delete(recordEntity2);

        //when
        FeedInfoResponse response = feedService.getFeed(Optional.of(userEntity.getId()), feedEntity.getId());

        //then
        Assertions.assertThat(response.getRecords())
                .hasSize(2)
                .extracting("id")
                .containsExactly(recordEntity1.getId(), recordEntity3.getId());
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 조회하면 예외가 발생한다")
    void getFeedByNotExistingFeedIdTest() throws Exception {
        //given
        Long notExistingFeedId = 0L;
        Long notExistingUserId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.getFeed(Optional.of(notExistingUserId), notExistingFeedId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("시용자가 피드를 생성하면 FeedCreateResponse을 반환한다")
    void createFeedByExistingUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        String feedName = "name";
        String imageUrl = "image";
        String companion = "companion1 companion2";
        LocalDateTime startAt = LocalDateTime.of(2022, 12, 25, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2022, 12, 30, 0, 0);
        String place = "jeju";
        String satisfaction = "good";
        String description = "description";

        FeedCreateRequest request = FeedCreateRequest.builder()
                .name(feedName)
                .companion(companion)
                .imageUrl(imageUrl)
                .description(description)
                .startAt(startAt)
                .endAt(endAt)
                .place(place)
                .satisfaction(satisfaction)
                .build();

        //when
        FeedCreateResponse response = feedService.createFeed(savedUserEntity.getId(), request);

        //then
        Assertions.assertThat(feedRepository.findById(response.getFeedId())).isPresent();
    }

    @Test
    @DisplayName("존재하지 않은 사용자 아이디로 피드를 생성하려고 하면 예외가 발생한다")
    void createFeedByNotExistingUserTest() throws Exception {
        // given
        FeedCreateRequest request = FeedCreateRequest.builder().build();

        //when // then
        Assertions.assertThatThrownBy(() -> feedService.createFeed(-1L, request)).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 피드를 수정하려고 하면 예외가 발생한다")
    void updateFeedWithNotExistingFeedIdTest() throws Exception {
        //given
        long userId = 1L;
        Long notExistingFeedId = 0L;

        FeedUpdateRequest request = FeedUpdateRequest.builder().build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.updateFeed(userId, notExistingFeedId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 피드를 수정하려고 하면 예외가 발생한다")
    void updateFeedWithNotWriterUserIdTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test1@email.com"));
        UserEntity other = userRepository.save(createUser("test2@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(author, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .name("updateFeedName")
                .imageUrl("updatedFeedImage")
                .description("updatedFeedDescription")
                .startAt(LocalDateTime.of(2022, 9, 1, 0, 0))
                .endAt(LocalDateTime.of(2022, 9, 1, 0, 0))
                .build();


        //when //then
        Assertions.assertThatThrownBy(() -> feedService.updateFeed(other.getId(), feedEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("피드 관리자가 피드 수정 요청을 하면 피드를 수정한다")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feed = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        String updateFeedName = "updated name";
        String updatedFeedImage = "updated feed image url";
        String updatedFeedDescription = "updated feed description";
        LocalDateTime updatedStartAt = LocalDateTime.of(2022, 9, 1, 0, 0);
        LocalDateTime updatedEndAt = LocalDateTime.of(2022, 9, 30, 0, 0);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .name(updateFeedName)
                .imageUrl(updatedFeedImage)
                .description(updatedFeedDescription)
                .startAt(updatedStartAt)
                .endAt(updatedEndAt)
                .build();

        //when
        feedService.updateFeed(userEntity.getId(), feed.getId(), request);

        //then
        Assertions.assertThat(feedRepository.findById(feed.getId()))
                .isPresent()
                .hasValueSatisfying(
                        feedEntity -> {
                            Assertions.assertThat(feedEntity.getName()).isEqualTo(updateFeedName);
                            Assertions.assertThat(feedEntity.getImageUrl()).isEqualTo(updatedFeedImage);
                            Assertions.assertThat(feedEntity.getDescription()).isEqualTo(updatedFeedDescription);
                            Assertions.assertThat(feedEntity.getStartAt()).isEqualTo(updatedStartAt);
                            Assertions.assertThat(feedEntity.getEndAt()).isEqualTo(updatedEndAt);
                        }
                );
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 피드 기록 리스트를 조회하면 예외가 발생한다")
    void getFeedRecordsWhenFeedNotFoundTest() throws Exception {
        //given
        long notExistingFeedId = 0L;
        PageRequest page = PageRequest.of(0, 10);

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.getFeedRecords(notExistingFeedId, page))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("피드 아이디로 기록 리스트를 페이지네이션으로 조회한다")
    void getFeedRecordsTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2021, 9, 30, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, feedTime, feedTime));
        RecordEntity recordEntity1 = createRecord(feedEntity);
        RecordEntity recordEntity2 = createRecord(feedEntity);
        RecordEntity recordEntity3 = createRecord(feedEntity);
        RecordEntity recordEntity4 = createRecord(feedEntity);
        RecordEntity recordEntity5 = createRecord(feedEntity);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4, recordEntity5));

        PageRequest page = PageRequest.of(0, 2);

        //when
        Page<FeedRecordsResponse> response = feedService.getFeedRecords(feedEntity.getId(), page);

        //then
        Assertions.assertThat(response.getTotalPages()).isEqualTo(3);
        Assertions.assertThat(response.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(response.getContent()).hasSize(2);
        Assertions.assertThat(response.getNumberOfElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("피드를 soft delete한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity savedFeedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecord(savedFeedEntity);
        RecordEntity recordEntity2 = createRecord(savedFeedEntity);
        RecordEntity recordEntity3 = createRecord(savedFeedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        feedService.deleteFeed(savedUserEntity.getId(), savedFeedEntity.getId());

        //then
        Assertions.assertThat(feedRepository.findAll()).isEmpty();
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("피드 삭제 권한이 없으면 예외가 발생한다")
    void deleteFeedWhenPermissionNotExistsTest() throws Exception {
        //given
        UserEntity owner = createUser("test@email.com");
        UserEntity other = createUser("test1@email.com");
        userRepository.saveAll(List.of(owner, other));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.deleteFeed(other.getId(), feedEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("title")
                .place("place")
                .date(LocalDateTime.now())
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }

}