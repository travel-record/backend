package world.trecord.service.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.userrecordlike.response.UserRecordLikeResponse;
import world.trecord.dto.userrecordlike.response.UserRecordLikedResponse;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.fixture.UserRecordLikeFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@Transactional
class UserRecordLikeServiceTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자가 좋아요한 기록에 좋아요를 하면 liked=false 응답을 한다")
    void toggleLikeTestWhenUserAlreadyLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        userRecordLikeRepository.save(UserRecordLikeFixture.of(userEntity, recordEntity));

        //when
        UserRecordLikedResponse response = userRecordLikeService.toggleLike(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(response.isLiked()).isFalse();
        Assertions.assertThat(userRecordLikeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요하지 않은 기록에 좋아요를 하면 liked=true 응답을 한다")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        //when
        UserRecordLikedResponse response = userRecordLikeService.toggleLike(userEntity.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(response.isLiked()).isTrue();
        Assertions.assertThat(userRecordLikeRepository.findAll())
                .hasSize(1)
                .extracting("userEntity", "recordEntity")
                .containsExactly(
                        tuple(userEntity, recordEntity)
                );
    }

    @Test
    @DisplayName("기록 작성자가 아닌 사용자가 기록에 좋아요를 하면 비동기로 기록 작성자를 향한 좋아요 알림을 생성한다")
    void createNotificationTestWhenViewerLikeOnRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity viewer = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(writer));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        //when
        userRecordLikeService.toggleLike(viewer.getId(), recordEntity.getId());

        //then
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Mockito.verify(mockEventListener, Mockito.times(1)).handleNotificationEventListener(Mockito.any()));
    }

    @Test
    @DisplayName("기록 작성자가 본인이 작성하였고 좋아요하지 않은 기록에 좋아요를 하면 기록 작성자를 향한 좋아요 알림을 생성하지 않는다")
    void createNotificationTestWhenWriterLikeOnRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(writer));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity));

        //when
        userRecordLikeService.toggleLike(writer.getId(), recordEntity.getId());

        //then
        Assertions.assertThat(notificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트를 조회하여 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity3 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity4 = RecordEntityFixture.of(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = UserRecordLikeFixture.of(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = UserRecordLikeFixture.of(userEntity, recordEntity4);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        int pageNumber = 0;
        int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<UserRecordLikeResponse> page = userRecordLikeService.getUserRecordLikeList(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent())
                .hasSize(pageSize)
                .extracting("recordId", "title", "authorNickname", "imageUrl")
                .containsOnly(
                        tuple(recordEntity4.getId(), recordEntity4.getTitle(), userEntity.getNickname(), recordEntity4.getImageUrl()),
                        tuple(recordEntity1.getId(), recordEntity1.getTitle(), userEntity.getNickname(), recordEntity1.getImageUrl())
                );
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록이 없으면 UserRecordLikeListResponse의 records 필드를 빈 배열로 반환한다")
    void getUserRecordLikeListWithEmptyListByTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));

        //when
        Page<UserRecordLikeResponse> page = userRecordLikeService.getUserRecordLikeList(userEntity.getId(), PageRequest.of(0, 10));

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }
    
    @Test
    @DisplayName("사용자가 좋아요한 기록 리스트에서 soft delete한 좋아요 리스트를 제외한 UserRecordLikeListResponse로 반환한다")
    void getUserRecordLikeListByWhenUserLikedCancelTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        UserRecordLikeEntity userRecordLikeEntity1 = UserRecordLikeFixture.of(other, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = UserRecordLikeFixture.of(other, recordEntity2);

        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        userRecordLikeRepository.delete(userRecordLikeEntity2);

        int pageNumber = 0;
        int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<UserRecordLikeResponse> page = userRecordLikeService.getUserRecordLikeList(other.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent())
                .hasSize(1)
                .extracting("recordId")
                .containsOnly(recordEntity1.getId());
    }
}