package world.trecord.service.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorStatus;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.feed.request.FeedCreateRequest;
import world.trecord.dto.feed.request.FeedUpdateRequest;
import world.trecord.dto.feed.response.FeedCreateResponse;
import world.trecord.dto.feed.response.FeedInfoResponse;
import world.trecord.dto.feed.response.FeedListResponse;
import world.trecord.dto.feed.response.FeedRecordsResponse;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;
import static world.trecord.exception.CustomExceptionError.FEED_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.FORBIDDEN;

@Transactional
class FeedServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자가 등록한 여행 시작 시간 내림차순으로 정렬된 피드 리스트를 반환한다")
    void getFeedListByUserId() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        FeedEntity feedEntity4 = createFeed(savedUserEntity, LocalDateTime.of(2021, 12, 21, 0, 0), LocalDateTime.of(2021, 12, 25, 0, 0));
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        final int pageNumber = 0;
        final int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<FeedListResponse> page = feedService.getFeedList(savedUserEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("사용자 등록한 피드가 없다면 빈 배열을 반환한다")
    void getEmptyFeedListByUserId() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        final int pageNumber = 0;
        final int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<FeedListResponse> page = feedService.getFeedList(savedUserEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("피드 아이디로 현재 참여 중인 피드 컨트리뷰터와 함께 피드 조회한다")
    void getFeed_withFeedContributors() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity contributor1 = UserEntityFixture.of();
        UserEntity contributor2 = UserEntityFixture.of();
        UserEntity contributor3 = UserEntityFixture.of();
        UserEntity contributor4 = UserEntityFixture.of();
        UserEntity contributor5 = UserEntityFixture.of();
        UserEntity contributor6 = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, contributor1, contributor2, contributor3, contributor4, contributor5, contributor6));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));

        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(contributor1, feedEntity);
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(contributor2, feedEntity);
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(contributor3, feedEntity);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(contributor4, feedEntity);
        FeedContributorEntity feedContributor5 = FeedContributorFixture.of(contributor5, feedEntity);
        FeedContributorEntity feedContributor6 = FeedContributorFixture.of(contributor6, feedEntity);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4, feedContributor5, feedContributor6));

        feedContributorService.leaveFeed(contributor5.getId(), feedEntity.getId()); // feed leave
        feedContributorService.expelUserFromFeed(owner.getId(), contributor6.getId(), feedEntity.getId()); // feed expel

        //when
        FeedInfoResponse response = feedService.getFeed(owner.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(response.getContributors())
                .hasSize(5)
                .extracting("userId", "nickname", "imageUrl")
                .containsOnly(
                        tuple(owner.getId(), owner.getNickname(), owner.getImageUrl()),
                        tuple(contributor1.getId(), contributor1.getNickname(), contributor1.getImageUrl()),
                        tuple(contributor2.getId(), contributor2.getNickname(), contributor2.getImageUrl()),
                        tuple(contributor3.getId(), contributor3.getNickname(), contributor3.getImageUrl()),
                        tuple(contributor4.getId(), contributor4.getNickname(), contributor4.getImageUrl()));
    }

    @Test
    @DisplayName("사용자가 등록한 특정 피드를 반환한다")
    void getFeedByFeedIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        //when
        FeedInfoResponse response = feedService.getFeed(userEntity.getId(), feedEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("writerId", "feedId", "startAt", "endAt")
                .containsExactly(userEntity.getId(), feedEntity.getId(),
                        feedEntity.convertStartAtToLocalDate(), feedEntity.convertEndAtToLocalDate());
    }

    @Test
    @DisplayName("인증되지 않은 사용자도 피드를 조회할 수 있다")
    void getFeed_byAnonymousUser_returnResponse() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));


        //when
        FeedInfoResponse response = feedService.getFeed(null, feedEntity.getId());

        //then
        Assertions.assertThat(response)
                .extracting("writerId", "feedId", "startAt", "endAt")
                .containsExactly(userEntity.getId(), feedEntity.getId(),
                        feedEntity.convertStartAtToLocalDate(), feedEntity.convertEndAtToLocalDate());
    }

    @Test
    @DisplayName("사용자가 soft delete한 피드는 페이지네이션에서 제외한다")
    void getFeedByFeedIdWhenFeedSoftDeletedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));
        feedRepository.delete(feedEntity3);

        final int pageNumber = 0;
        final int pageSize = 5;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<FeedListResponse> page = feedService.getFeedList(savedUserEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).hasSize(2);
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
                .isEqualTo(FEED_NOT_FOUND);
    }

    @Test
    @DisplayName("시용자가 피드를 생성하면 FeedCreateResponse을 반환한다")
    void createFeedByExistingUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        String feedName = "name";
        String imageUrl = "image";
        LocalDateTime startAt = LocalDateTime.of(2022, 12, 25, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2022, 12, 30, 0, 0);
        String place = "jeju";
        String satisfaction = "good";
        String description = "description";

        FeedCreateRequest request = FeedCreateRequest.builder()
                .name(feedName)
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
        UserEntity author = userRepository.save(UserEntityFixture.of("test1@email.com"));
        UserEntity other = userRepository.save(UserEntityFixture.of("test2@email.com"));

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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2021, 9, 30, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, feedTime, feedTime));
        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity3 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity4 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity5 = RecordEntityFixture.of(feedEntity);
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
        UserEntity savedUserEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        FeedEntity savedFeedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = RecordEntityFixture.of(savedFeedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(savedFeedEntity);
        RecordEntity recordEntity3 = RecordEntityFixture.of(savedFeedEntity);

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
        UserEntity owner = UserEntityFixture.of("test@email.com");
        UserEntity other = UserEntityFixture.of("test1@email.com");
        userRepository.saveAll(List.of(owner, other));

        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.deleteFeed(other.getId(), feedEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("가장 최근에 피드에서 추방된 사용자는 피드를 조회할 수 없다")
    void getFeed_whenUserExpelled_throwsForbiddenException() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity contributor = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, contributor));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity)); // 참여 중
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(contributor.getId(), feedEntity.getId(), FeedContributorStatus.EXPELLED);
        entityManager.flush();
        entityManager.clear();

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.getFeed(contributor.getId(), feedEntity.getId()))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(FORBIDDEN);
    }

    @CsvSource({"LEFT", "EXPELLED"})
    @ParameterizedTest
    @DisplayName("예전에 피드에 나갔다가 다시 참여하는 사용자는 피드를 조회할 수 있다")
    void getFeed_whenUserHasLeavedOrExpelledBefore_pass(FeedContributorStatus status) throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of();
        UserEntity contributor = UserEntityFixture.of();
        userRepository.saveAll(List.of(owner, contributor));

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(contributor.getId(), feedEntity.getId(), status);
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        entityManager.flush();
        entityManager.clear();

        //when //then
        feedService.getFeed(contributor.getId(), feedEntity.getId());
    }


    @Test
    @DisplayName("피드 등록 시 컨트리뷰터와 함께 등록한다")
    void createFeed_whenContributorsExist_returnResponse() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity invitee1 = UserEntityFixture.of();
        UserEntity invitee2 = UserEntityFixture.of();
        UserEntity invitee3 = UserEntityFixture.of();
        UserEntity invitee4 = UserEntityFixture.of();
        UserEntity invitee5 = UserEntityFixture.of();
        List<UserEntity> invitees = List.of(invitee1, invitee2, invitee3, invitee4, invitee5);
        userRepository.saveAll(invitees);

        FeedCreateRequest request = FeedCreateRequest.builder()
                .name("name")
                .imageUrl("image")
                .description("description")
                .startAt(LocalDateTime.of(2022, 12, 25, 0, 0))
                .endAt(LocalDateTime.of(2022, 12, 30, 0, 0))
                .place("jeju")
                .satisfaction("good")
                .contributors(List.of(invitee1.getId(), invitee2.getId(), invitee3.getId(), invitee4.getId(), invitee5.getId()))
                .build();

        //when
        FeedCreateResponse response = feedService.createFeed(owner.getId(), request);

        //then
        Assertions.assertThat(feedRepository.findAll()).hasSize(1);
        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(invitees.size());
    }

    @Test
    @DisplayName("피드 등록 시 컨트리뷰터가 DB에서 조회되지 않으면 예외가 발생한다")
    void createFeed_whenContributorsNotFound_throwException() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity invitee1 = UserEntityFixture.of();
        UserEntity invitee2 = UserEntityFixture.of();
        UserEntity invitee3 = UserEntityFixture.of();
        UserEntity invitee4 = UserEntityFixture.of();
        UserEntity invitee5 = UserEntityFixture.of();
        List<UserEntity> invitees = List.of(invitee1, invitee2, invitee3, invitee4, invitee5);
        userRepository.saveAll(invitees);
        long notExistUserId = -1L;

        FeedCreateRequest request = FeedCreateRequest.builder()
                .name("name")
                .imageUrl("image")
                .description("description")
                .startAt(LocalDateTime.of(2022, 12, 25, 0, 0))
                .endAt(LocalDateTime.of(2022, 12, 30, 0, 0))
                .place("jeju")
                .satisfaction("good")
                .contributors(List.of(notExistUserId, invitee1.getId(), invitee2.getId(), invitee3.getId(), invitee4.getId(), invitee5.getId()))
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> feedService.createFeed(owner.getId(), request))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }
}