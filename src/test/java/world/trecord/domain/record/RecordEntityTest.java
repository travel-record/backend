package world.trecord.domain.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.domain.feed.FeedEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

class RecordEntityTest {

    @Test
    @DisplayName("date를 LocalDate로 변환한다")
    void convertDateToLocalDateTest() throws Exception {
        //given
        LocalDateTime date = LocalDateTime.of(2022, 3, 1, 0, 0);
        RecordEntity recordEntity = RecordEntity.builder()
                .date(date)
                .build();

        //when
        LocalDate convertedDate = recordEntity.convertDateToLocalDate();

        //then
        Assertions.assertThat(convertedDate).isEqualTo(date.toLocalDate());
    }

    @Test
    @DisplayName("기록의 필드값을 수정하면 수정된 값을 가진다")
    void updateTest() throws Exception {
        //given
        RecordEntity recordEntity = RecordEntity.builder()
                .build();

        String updatedContent = "updated content";
        RecordEntity updateEntity = RecordEntity.builder()
                .content(updatedContent)
                .build();

        //when
        recordEntity.update(updateEntity);

        //then
        Assertions.assertThat(recordEntity.getContent()).isEqualTo(updatedContent);
    }

    @Test
    @DisplayName("같은 피드를 가지면 true를 반환한다")
    void hasSameFeedEntityReturnsTrueTest() throws Exception {
        //given
        FeedEntity feedEntity = FeedEntity.builder().build();

        ReflectionTestUtils.setField(feedEntity, "id", 1L);

        RecordEntity recordEntity1 = RecordEntity.builder()
                .feedEntity(feedEntity)
                .build();

        RecordEntity recordEntity2 = RecordEntity.builder()
                .feedEntity(feedEntity)
                .build();

        //when
        boolean result = recordEntity1.hasSameFeedEntity(recordEntity2);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 피드를 가지면 false를 반환한다")
    void hasSameFeedEntityReturnsFalseTest() throws Exception {
        //given
        FeedEntity feedEntity1 = FeedEntity.builder().build();
        ReflectionTestUtils.setField(feedEntity1, "id", 1L);

        RecordEntity recordEntity1 = RecordEntity.builder()
                .feedEntity(feedEntity1)
                .build();

        FeedEntity feedEntity2 = FeedEntity.builder().build();
        ReflectionTestUtils.setField(feedEntity2, "id", 2L);

        RecordEntity recordEntity2 = RecordEntity.builder()
                .feedEntity(feedEntity2)
                .build();

        //when
        boolean result = recordEntity1.hasSameFeedEntity(recordEntity2);

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("기록 순서를 변경한다")
    void swapSequenceWithTest() throws Exception {
        //given
        int seq1 = 1;
        RecordEntity recordEntity1 = RecordEntity.builder()
                .sequence(seq1)
                .build();

        int seq2 = 2;
        RecordEntity recordEntity2 = RecordEntity.builder()
                .sequence(seq2)
                .build();

        //when
        recordEntity1.swapSequenceWith(recordEntity2);

        //then
        Assertions.assertThat(recordEntity1.getSequence()).isEqualTo(seq2);
        Assertions.assertThat(recordEntity2.getSequence()).isEqualTo(seq1);
    }

}