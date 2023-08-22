package world.trecord.domain.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

}