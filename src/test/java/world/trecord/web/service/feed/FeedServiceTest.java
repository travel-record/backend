package world.trecord.web.service.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.web.exception.CustomException;
import world.trecord.web.exception.CustomExceptionError;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;
import world.trecord.web.service.feed.response.FeedCreateResponse;
import world.trecord.web.service.feed.response.FeedInfoResponse;
import world.trecord.web.service.feed.response.FeedListResponse;
import world.trecord.web.service.feed.response.FeedUpdateResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class FeedServiceTest extends ContainerBaseTest {

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
        UserEntity savedUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity1 = createFeedEntity(savedUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeedEntity(savedUserEntity, "feed name2", LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeedEntity(savedUserEntity, "feed name3", LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        FeedEntity feedEntity4 = createFeedEntity(savedUserEntity, "feed name4", LocalDateTime.of(2021, 12, 21, 0, 0), LocalDateTime.of(2021, 12, 25, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        //when
        FeedListResponse feedListResponse = feedService.getFeedList(savedUserEntity.getId());

        //then
        Assertions.assertThat(feedListResponse.getFeeds()).extracting("name", "startAt")
                .containsExactly(
                        tuple("feed name4", LocalDate.of(2021, 12, 21)),
                        tuple("feed name3", LocalDate.of(2021, 12, 10)),
                        tuple("feed name2", LocalDate.of(2021, 10, 4)),
                        tuple("feed name1", LocalDate.of(2021, 9, 30))
                );
    }

    @Test
    @DisplayName("사용자 등록한 피드가 없다면 빈 배열을 반환한다")
    void getEmptyFeedListByUserId() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        //when
        FeedListResponse feedListResponse = feedService.getFeedList(savedUserEntity.getId());

        //then
        Assertions.assertThat(feedListResponse.getFeeds()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 등록한 특정 피드를 기록과 함께 반환한다")
    void getFeedByFeedIdTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity savedFeedEntity = feedRepository.save(createFeedEntity(savedUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecordEntity(savedFeedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(savedFeedEntity, "record2", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(savedFeedEntity, "record3", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        FeedInfoResponse response = feedService.getFeed(savedUserEntity.getId(), savedFeedEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("writerId", "feedId", "startAt", "endAt")
                .containsExactly(savedUserEntity.getId(), savedFeedEntity.getId(),
                        savedFeedEntity.convertStartAtToLocalDate(), savedFeedEntity.convertEndAtToLocalDate());
        Assertions.assertThat(response.getRecords().stream().map(FeedInfoResponse.Record::getTitle)).containsExactly("record3", "record1", "record2");
    }

    @Test
    @DisplayName("사용자가 soft delete한 피드는 반환하지 않는다")
    void getFeedByFeedIdWhenFeedSoftDeletedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity1 = createFeedEntity(savedUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeedEntity(savedUserEntity, "feed name2", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity3 = createFeedEntity(savedUserEntity, "feed name3", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));

        feedRepository.softDeleteById(feedEntity3.getId());

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
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 4, 0, 0)));

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 10, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place3", LocalDateTime.of(2022, 10, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place1", LocalDateTime.of(2022, 10, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        recordRepository.softDeleteById(recordEntity2.getId());

        //when
        FeedInfoResponse response = feedService.getFeed(userEntity.getId(), feedEntity.getId());

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
        Assertions.assertThatThrownBy(() -> feedService.getFeed(notExistingUserId, notExistingFeedId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("시용자가 피드를 생성하면 FeedCreateResponse을 반환한다")
    void createFeedByExistingUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        String feedName = "feed name";
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
                .isEqualTo(CustomExceptionError.NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 피드를 수정하려고 하면 예외가 발생한다")
    void updateFeedWithNotWriterUserIdTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntity.builder().email("test1@email.com").build());
        UserEntity other = userRepository.save(UserEntity.builder().email("test2@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(author, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

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
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("사용자가 피드를 수정하면 수정된 내용으로 응답한다")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        String updateFeedName = "updated feed name";
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
        FeedUpdateResponse response = feedService.updateFeed(userEntity.getId(), feedEntity.getId(), request);

        //then
        Assertions.assertThat(response)
                .extracting("writerId", "feedId", "name", "description", "startAt", "endAt")
                .containsExactly(userEntity.getId(), feedEntity.getId(), updateFeedName, updatedFeedDescription,
                        feedEntity.convertStartAtToLocalDate(), feedEntity.convertEndAtToLocalDate());

        Assertions.assertThat(response.getRecords().stream().map(FeedUpdateResponse.Record::getTitle)).containsExactly("record3", "record1", "record2");
    }

    @Test
    @DisplayName("피드를 soft delete한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity savedFeedEntity = feedRepository.save(createFeedEntity(savedUserEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecordEntity(savedFeedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(savedFeedEntity, "record2", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(savedFeedEntity, "record3", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        feedService.deleteFeed(savedUserEntity.getId(), savedFeedEntity.getId());

        //then
        Assertions.assertThat(feedRepository.findAll()).isEmpty();
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
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

}