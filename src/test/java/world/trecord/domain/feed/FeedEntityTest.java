package world.trecord.domain.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

class FeedEntityTest {

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

    @Test
    @DisplayName("피드 관리자이면 true를 반환한다")
    void isManagedByReturnsTrueTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        FeedEntity feedEntity = FeedEntity
                .builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = feedEntity.isOwnedBy(userEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("피드 관리자가 아니면 false를 반환한다")
    void isManagedByReturnsFalseTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity1, "id", 1L);

        UserEntity userEntity2 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity2, "id", 2L);

        FeedEntity feedEntity = FeedEntity
                .builder()
                .userEntity(userEntity2)
                .build();

        //when
        boolean result = feedEntity.isOwnedBy(userEntity1.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("피드 컨트리뷰터이면 true를 반환한다")
    void isContributorReturnsTrueTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        FeedEntity feedEntity = FeedEntity
                .builder()
                .build();

        FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();

        //when
        boolean result = feedEntity.isContributor(userEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("피드 컨트리뷰터가 아니면 false를 반환한다")
    void isContributorReturnsFalseTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity1, "id", 1L);

        UserEntity userEntity2 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity2, "id", 2L);

        FeedEntity feedEntity = FeedEntity
                .builder()
                .build();

        FeedContributorEntity.builder()
                .userEntity(userEntity2)
                .feedEntity(feedEntity)
                .build();

        //when
        boolean result = feedEntity.isContributor(userEntity1.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("피드 컨트리뷰터 집합에서 제거한다")
    void removeFeedContributorTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        FeedEntity feedEntity = FeedEntity
                .builder()
                .build();

        FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();

        //when
        feedEntity.removeFeedContributor(userEntity.getId());

        //then
        Assertions.assertThat(feedEntity.getFeedContributors()).isEmpty();
    }
}