package world.trecord.controller.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordUpdateRequest;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.time.LocalDateTime;

@Transactional
class RecordValidatorTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("기록을 생성할 때 date가 피드 시작 시간 전이면 BindException 예외가 발생한다")
    void verifyRecordCreateRequestTestWhenDateIsBeforeFeedStartAt() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntity.builder().email("test@email.com").build());

        LocalDateTime feedStartAt = LocalDateTime.of(2021, 9, 30, 0, 0);
        LocalDateTime feedEndAt = LocalDateTime.of(2021, 10, 2, 0, 0);

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt));

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

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt));

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

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt));

        LocalDateTime date = feedStartAt.minusDays(1);

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, date));

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

        FeedEntity feedEntity = feedRepository.save(createFeedEntity(userEntity, feedStartAt, feedEndAt));

        LocalDateTime date = feedEndAt.plusDays(1);

        RecordEntity recordEntity = recordRepository.save(createRecordEntity(feedEntity, date));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .date(date)
                .build();

        //when //then
        Assertions.assertThatThrownBy(() -> recordValidator.verify(recordEntity.getId(), request))
                .isInstanceOf(BindException.class);
    }

    private FeedEntity createFeedEntity(UserEntity saveUserEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name("feed name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, LocalDateTime date) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("record1")
                .place("place")
                .latitude("latitude")
                .longitude("longitude")
                .date(date)
                .content("content1")
                .weather("weather1")
                .transportation("satisfaction1")
                .feeling("feeling1")
                .build();
    }

}