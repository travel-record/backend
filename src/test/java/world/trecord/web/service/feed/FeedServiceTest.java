package world.trecord.web.service.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;
import world.trecord.web.service.feed.response.FeedCreateResponse;
import world.trecord.web.service.feed.response.FeedDeleteResponse;
import world.trecord.web.service.feed.response.FeedListResponse;
import world.trecord.web.service.feed.response.FeedOneResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class FeedServiceTest {

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
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity1 = createFeedEntity(savedUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeedEntity(savedUserEntity, "feed name2", LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeedEntity(savedUserEntity, "feed name3", LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        FeedEntity feedEntity4 = createFeedEntity(savedUserEntity, "feed name4", LocalDateTime.of(2021, 12, 21, 0, 0), LocalDateTime.of(2021, 12, 25, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        //when
        FeedListResponse feedListResponse = feedService.getFeedListBy(savedUserEntity.getId());

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
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        //when
        FeedListResponse feedListResponse = feedService.getFeedListBy(savedUserEntity.getId());

        //then
        Assertions.assertThat(feedListResponse.getFeeds()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 등록한 특정 피드를 기록과 함께 반환한다")
    void getFeedByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(savedUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        FeedOneResponse response = feedService.getFeedBy(savedFeedEntity.getId());

        //then
        Assertions.assertThat(response.getWriterId()).isEqualTo(savedUserEntity.getId());
        Assertions.assertThat(response.getFeedId()).isEqualTo(savedFeedEntity.getId());
        Assertions.assertThat(response.getStartAt()).isEqualTo(savedFeedEntity.getStartAt().toLocalDate());
        Assertions.assertThat(response.getEndAt()).isEqualTo(savedFeedEntity.getEndAt().toLocalDate());
        Assertions.assertThat(response.getRecords().stream().map(FeedOneResponse.Record::getTitle)).containsExactly("record3", "record1", "record2");
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 조회하면 예외가 발생한다")
    void getFeedByNotExistingFeedIdTest() throws Exception {
        //given
        Long notExistingFeedId = 0L;

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.getFeedBy(notExistingFeedId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("시용자가 피드를 생성하면 생성 응답으로 반환한다")
    void createFeedByExistingUserTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

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
        Assertions.assertThat(response.getFeedId()).isNotNull();
        Assertions.assertThat(response).extracting("name", "imageUrl", "companion", "description", "startAt", "endAt", "place", "satisfaction")
                .containsExactly(feedName, imageUrl, companion, description, startAt.toLocalDate(), endAt.toLocalDate(), place, satisfaction);
    }

    @Test
    @DisplayName("존재하지 않은 사용자 아이디로 피드를 생성하려고 하면 예외가 발생한다")
    void createFeedByNotExistingUserTest() throws Exception {
        // given
        FeedCreateRequest request = FeedCreateRequest.builder()
                .build();

        //when // then
        Assertions.assertThatThrownBy(() -> feedService.createFeed(-1L, request)).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 피드를 수정하려고 하면 예외가 발생한다")
    void updateFeedWithNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;
        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.updateFeed(notExistingUserId, request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

    @Test
    @DisplayName("존재하지 않는 피드 아이디로 피드를 수정하려고 하면 예외가 발생한다")
    void updateFeedWithNotExistingFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .name("updateFeedName")
                .imageUrl("updatedFeedImage")
                .description("updatedFeedDescription")
                .startAt(LocalDateTime.of(2022, 9, 1, 0, 0))
                .endAt(LocalDateTime.of(2022, 9, 1, 0, 0))
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.updateFeed(savedUserEntity.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_FEED);
    }

    @Test
    @DisplayName("피드 작성자가 아닌 사용자가 피드를 수정하려고 하면 예외가 발생한다")
    void updateFeedWithNotWriterUserIdTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntity.builder()
                .email("test1@email.com")
                .build();
        UserEntity writerUser = userRepository.save(userEntity1);

        UserEntity userEntity2 = UserEntity.builder()
                .email("test2@email.com")
                .build();
        UserEntity strangerUser = userRepository.save(userEntity2);

        FeedEntity feedEntity = createFeedEntity(writerUser, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .id(savedFeedEntity.getId())
                .name("updateFeedName")
                .imageUrl("updatedFeedImage")
                .description("updatedFeedDescription")
                .startAt(LocalDateTime.of(2022, 9, 1, 0, 0))
                .endAt(LocalDateTime.of(2022, 9, 1, 0, 0))
                .build();


        //when //then
        Assertions.assertThatThrownBy(() -> feedService.updateFeed(strangerUser.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.FORBIDDEN);
    }

    @Test
    @DisplayName("사용자가 피드를 수정하면 수정 응답으로 반환한다")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(savedUserEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

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
                .id(feedEntity.getId())
                .name(updateFeedName)
                .imageUrl(updatedFeedImage)
                .description(updatedFeedDescription)
                .startAt(updatedStartAt)
                .endAt(updatedEndAt)
                .build();

        //when
        FeedOneResponse response = feedService.updateFeed(savedUserEntity.getId(), request);

        //then
        Assertions.assertThat(response.getWriterId()).isEqualTo(savedUserEntity.getId());
        Assertions.assertThat(response.getFeedId()).isEqualTo(savedFeedEntity.getId());
        Assertions.assertThat(response.getName()).isEqualTo(updateFeedName);
        Assertions.assertThat(response.getDescription()).isEqualTo(updatedFeedDescription);
        Assertions.assertThat(response.getStartAt()).isEqualTo(updatedStartAt.toLocalDate());
        Assertions.assertThat(response.getEndAt()).isEqualTo(updatedEndAt.toLocalDate());
        Assertions.assertThat(response.getRecords().stream().map(FeedOneResponse.Record::getTitle)).containsExactly("record3", "record1", "record2");
    }

    @Test
    @DisplayName("피드를 삭제하면 하위 기록과 함께 삭제되고 삭제된 피드 아이디를 반환한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(savedUserEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when
        FeedDeleteResponse response = feedService.deleteFeed(savedUserEntity.getId(), savedFeedEntity.getId());

        //then
        Assertions.assertThat(response.getId()).isEqualTo(savedFeedEntity.getId());
        Assertions.assertThat(feedRepository.findById(savedFeedEntity.getId())).isEmpty();
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