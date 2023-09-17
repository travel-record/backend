package world.trecord.controller.feed;

import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import world.trecord.dto.feed.request.FeedCreateRequest;
import world.trecord.dto.feed.request.FeedUpdateRequest;

@Component
public class FeedValidator {

    public void verify(FeedCreateRequest request) throws BindException {

        if (request.getStartAt().isAfter(request.getEndAt())) {
            BindException bindException = new BindException(request, "feedCreateRequest");
            bindException.addError(new FieldError("feedCreateRequest", "startAt", "Start time should be before end time."));
            throw bindException;
        }
    }

    public void verify(FeedUpdateRequest request) throws BindException {

        if (request.getStartAt().isAfter(request.getEndAt())) {
            BindException bindException = new BindException(request, "feedUpdateRequest");
            bindException.addError(new FieldError("feedUpdateRequest", "startAt", "Start time should be before end time."));
            throw bindException;
        }
    }
}
