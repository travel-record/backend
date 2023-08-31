package world.trecord.web.controller.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.IntegrationContainerBaseTest;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;

import java.time.LocalDateTime;

class RecordValidatorTest extends IntegrationContainerBaseTest {

    @Autowired
    RecordValidator recordValidator;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @DisplayName("기록을 생성할 때 date가 피드 시작 시간 전이면 BindException 예외가 발생한다")
    void verifyRecordCreateRequestTestWhenDateIsBeforeFeedStartAt() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2021, 9, 30, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2021, 10, 2, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", feedStartAt, feedEndAt));

        LocalDateTime date = feedStartAt.minusDays(1);

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .date(date)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordValidator.verify(request))
                .isInstanceOf(BindException.class);
    }

    @Test
    @DisplayName("기록을 생성할 때 date가 피드 종료 시간 후이면 BindException 예외가 발생한다")
    void verifyRecordCreateRequestTestWhenDateIsAfterFeedEndA() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2021, 9, 30, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2021, 10, 2, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", feedStartAt, feedEndAt));

        LocalDateTime date = feedEndAt.plusDays(1);

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .date(date)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordValidator.verify(request))
                .isInstanceOf(BindException.class);
    }

    @Test
    @DisplayName("기록을 수정할 때 date가 피드 시작 시간 전이면 BindException 예외가 발생한다")
    void verifyRecordUpdateRequestTestWhenDateIsBeforeFeedStartAt() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2021, 9, 30, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2021, 10, 2, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", feedStartAt, feedEndAt));

        LocalDateTime date = feedStartAt.minusDays(1);

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", date, "content1", "weather1", "satisfaction1", "feeling1"));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .date(date)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordValidator.verify(recordEntity.getId(), request))
                .isInstanceOf(BindException.class);
    }

    @Test
    @DisplayName("기록을 수정할 때 date가 피드 종료 시간 후이면 BindException 예외가 발생한다")
    void verifyRecordUpdateRequestTestWhenDateIsAfterFeedEndA() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2021, 9, 30, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2021, 10, 2, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, "feed name", feedStartAt, feedEndAt));

        LocalDateTime date = feedEndAt.plusDays(1);

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, "record1", "place2", date, "content1", "weather1", "satisfaction1", "feeling1"));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .date(date)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordValidator.verify(recordEntity.getId(), request))
                .isInstanceOf(BindException.class);
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