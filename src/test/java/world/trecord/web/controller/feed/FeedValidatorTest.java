package world.trecord.web.controller.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;

import java.time.LocalDateTime;

class FeedValidatorTest {

    @Test
    @DisplayName("피드 생성 요청할 때 여행 시작 시간이 여행 종료 시간보다 뒤에 있으면 예외가 발생한다")
    void validateFeedCreateRequestTest() throws Exception {
        //given
        FeedCreateRequest request = FeedCreateRequest.builder()
                .startAt(LocalDateTime.of(2022, 10, 31, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 1, 0, 0))
                .build();

        FeedValidator feedValidator = new FeedValidator();

        //when //then
        Assertions.assertThatThrownBy(() -> feedValidator.validateFeedCreateRequest(request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("피드 업데이트 요청할 때 여행 시작 시간이 여행 종료 시간보다 뒤에 있으면 예외가 발생한다")
    void validateFeedUpdateRequestTest() throws Exception {
        //given
        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .startAt(LocalDateTime.of(2022, 10, 31, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 1, 0, 0))
                .build();

        FeedValidator feedValidator = new FeedValidator();

        //when //then
        Assertions.assertThatThrownBy(() -> feedValidator.validateFeedUpdateRequest(request))
                .isInstanceOf(CustomException.class);
    }

}