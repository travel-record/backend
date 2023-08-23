package world.trecord.web.controller.feed;

import org.springframework.stereotype.Component;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;

import static world.trecord.web.exception.CustomExceptionError.INVALID_ARGUMENT;

@Component
public class FeedValidator {

    public void verify(FeedCreateRequest request) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            return;
        }

        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new CustomException(INVALID_ARGUMENT);
        }
    }

    public void verify(FeedUpdateRequest request) {
        if (request.getStartAt() == null || request.getEndAt() == null) {
            return;
        }

        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new CustomException(INVALID_ARGUMENT);
        }
    }
}
