package world.trecord.domain.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

class FeedEntityTest {

    @Test
    @DisplayName("기록을 날짜 순으로 정렬한다")
    void sortRecordEntitiesByDateAndCreatedTimeAscTest() throws Exception {
        //given
        FeedEntity feedEntity = FeedEntity.builder()
                .build();

        RecordEntity recordEntity4 = createRecordEntity(feedEntity, LocalDateTime.of(2022, 3, 4, 0, 0));
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, LocalDateTime.of(2022, 3, 3, 0, 0));
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity1 = createRecordEntity(feedEntity, LocalDateTime.of(2022, 3, 1, 0, 0));

        //when
        List<RecordEntity> recordEntities = feedEntity.sortRecordEntitiesByDateAndCreatedTimeAsc().toList();

        //then
        Assertions.assertThat(recordEntities)
                .hasSize(4)
                .containsExactly(recordEntity1, recordEntity2, recordEntity3, recordEntity4);
    }

    @Test
    @DisplayName("여행 시작 날짜를 LocalDate로 변환한다")
    void convertStartAtToLocalDateTest() throws Exception {
        //given
        LocalDateTime startDate = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = FeedEntity.builder()
                .startAt(startDate)
                .build();

        //when
        LocalDate convertedLocalDate = feedEntity.convertStartAtToLocalDate();

        //then
        Assertions.assertThat(convertedLocalDate).isEqualTo(startDate.toLocalDate());
    }

    @Test
    @DisplayName("여행 종료 날짜를 LocalDate로 변환한다")
    void convertEndAtToLocalDateTest() throws Exception {
        LocalDateTime endDate = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = FeedEntity.builder()
                .startAt(endDate)
                .build();

        //when
        LocalDate convertedLocalDate = feedEntity.convertStartAtToLocalDate();

        //then
        Assertions.assertThat(convertedLocalDate).isEqualTo(endDate.toLocalDate());
    }

    @Test
    @DisplayName("피드의 필드값을 수정하면 수정된 값을 가진다")
    void updateTest() throws Exception {
        //given
        FeedEntity feedEntity = FeedEntity.builder()
                .build();

        String updateDescription = "updated description";
        FeedEntity updateEntity = FeedEntity.builder()
                .description(updateDescription)
                .build();

        //when
        feedEntity.update(updateEntity);

        //then
        Assertions.assertThat(feedEntity.getDescription()).isEqualTo(updateDescription);
    }

    @Test
    @DisplayName("같은 피드이면 true를 반환한다")
    void isSameFeedEntityReturnsTrueTest() throws Exception {
        //given
        FeedEntity feedEntity1 = FeedEntity.builder().build();

        ReflectionTestUtils.setField(feedEntity1, "id", 1L);

        FeedEntity feedEntity2 = FeedEntity.builder().build();

        ReflectionTestUtils.setField(feedEntity2, "id", 1L);

        //when
        boolean result = feedEntity1.isEqualTo(feedEntity2);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 피드이면 false를 반환한다")
    void isSameFeedEntityReturnsFalseTest() throws Exception {
        //given
        FeedEntity feedEntity1 = FeedEntity.builder().build();

        ReflectionTestUtils.setField(feedEntity1, "id", 1L);

        FeedEntity feedEntity2 = FeedEntity.builder().build();

        ReflectionTestUtils.setField(feedEntity2, "id", 2L);

        //when
        boolean result = feedEntity1.isEqualTo(feedEntity2);

        //then
        Assertions.assertThat(result).isFalse();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, LocalDateTime date) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .date(date)
                .build();
    }

}