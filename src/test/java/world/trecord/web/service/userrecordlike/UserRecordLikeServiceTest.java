package world.trecord.web.service.userrecordlike;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@IntegrationTestSupport
class UserRecordLikeServiceTest {

    @Autowired
    UserRecordLikeService userRecordLikeService;

    @Autowired
    UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    FeedRepository feedRepository;


    @Test
    @DisplayName("사용자가 좋아요한 기록에 좋아요를 하면 isLiked=false 응답을 한다")
    void toggleLikeTestWhenUserAlreadyLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));
        userRecordLikeRepository.save(createUserRecordLikeEntity(userEntity, recordEntity));

        //when
        UserRecordLikeResponse response = userRecordLikeService.toggleLike(recordEntity.getId(), userEntity.getId());

        //then
        Assertions.assertThat(response.isLiked()).isFalse();
        Assertions.assertThat(userRecordLikeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("사용자가 좋아요한 기록에 좋아요를 하면 isLiked=true 응답을 한다")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email("test@email.com")
                .build());

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record", "place", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1"));

        //when
        UserRecordLikeResponse response = userRecordLikeService.toggleLike(recordEntity.getId(), userEntity.getId());

        //then
        Assertions.assertThat(response.isLiked()).isTrue();
        Assertions.assertThat(userRecordLikeRepository.findAll())
                .hasSize(1)
                .extracting("userEntity", "recordEntity")
                .containsExactly(
                        tuple(userEntity, recordEntity)
                );
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
        UserRecordLikeEntity userRecordLikeEntity = UserRecordLikeEntity
                .builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
        return userRecordLikeEntity;
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