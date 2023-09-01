package world.trecord.domain.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.time.LocalDateTime;
import java.util.List;

@IntegrationTestSupport
class FeedRepositoryTest extends ContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @DisplayName("유저 엔티티로 피드 리스트를 조회할 때 사용자가 등록한 피드 리스트를 여행 시작 시간 내림차순으로 조회한다")
    void findByUserEntityOrderByStartAtDescTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity saveUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity1 = createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeedEntity(saveUserEntity, "feed name2", LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeedEntity(saveUserEntity, "feed name3", LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3));

        //when
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userEntity.getId());

        //then
        Assertions.assertThat(feedEntities).extracting("name")
                .containsExactly(
                        "feed name3", "feed name2", "feed name1"
                );
    }

    @Test
    @DisplayName("유저 엔티티로 피드 리스트를 조회할 때 사용자가 등록한 피드가 없으면 빈 리스트가 반환된다")
    void findByUserEntityOrderByStartAtDescWithEmptyFeedListTest() throws Exception {
        //given
        UserEntity saveUserEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        //when
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(saveUserEntity.getId());

        //then
        Assertions.assertThat(feedEntities).isEmpty();
    }

    @Test
    @DisplayName("피드를 soft delete 한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity saveUserEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        FeedEntity savedFeedEntity = feedRepository.save(createFeedEntity(saveUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        //when
        feedRepository.softDeleteById(savedFeedEntity.getId());

        //then
        Assertions.assertThat(feedRepository.findAll()).isEmpty();
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }
}