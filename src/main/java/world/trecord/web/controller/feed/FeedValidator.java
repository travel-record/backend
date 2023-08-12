package world.trecord.web.controller.feed;

import org.springframework.stereotype.Component;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;

@Component
public class FeedValidator {

    public void validateFeedCreateRequest(FeedCreateRequest request) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            return;
        }

        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new CustomException(CustomExceptionError.INVALID_ARGUMENT);
        }
    }

    public void validateFeedUpdateRequest(FeedUpdateRequest request) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            return;
        }

        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new CustomException(CustomExceptionError.INVALID_ARGUMENT);
        }
    }
}
