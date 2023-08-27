package world.trecord.web.controller.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;

import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_FEED;
import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_RECORD;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class RecordValidator {

    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;

    public void verify(RecordCreateRequest request) throws BindException {
        FeedEntity feedEntity = feedRepository.findById(request.getFeedId()).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));

        String recordCreateRequest = "recordCreateRequest";
        String fieldDate = "date";

        if (feedEntity.getStartAt() != null && request.getDate().isBefore(feedEntity.getStartAt())) {
            BindException bindException = new BindException(request, recordCreateRequest);
            bindException.addError(new FieldError(recordCreateRequest, fieldDate, "Date should be after feed end time."));
            throw bindException;
        }

        if (feedEntity.getEndAt() != null && request.getDate().isAfter(feedEntity.getEndAt())) {
            BindException bindException = new BindException(request, recordCreateRequest);
            bindException.addError(new FieldError(recordCreateRequest, fieldDate, "Date should be after feed end time."));
            throw bindException;
        }
    }

    public void verify(Long recordId, RecordUpdateRequest request) throws BindException {

        RecordEntity recordEntity = recordRepository.findRecordEntityWithFeedEntityById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));

        FeedEntity feedEntity = recordEntity.getFeedEntity();

        String recordUpdateRequest = "recordUpdateRequest";

        String fieldDate = "date";

        if (feedEntity.getStartAt() != null && request.getDate().isBefore(feedEntity.getStartAt())) {
            BindException bindException = new BindException(request, recordUpdateRequest);
            bindException.addError(new FieldError(recordUpdateRequest, fieldDate, "Date should be after feed end time."));
            throw bindException;
        }

        if (feedEntity.getEndAt() != null && request.getDate().isAfter(feedEntity.getEndAt())) {
            BindException bindException = new BindException(request, recordUpdateRequest);
            bindException.addError(new FieldError(recordUpdateRequest, fieldDate, "Date should be after feed end time."));
            throw bindException;
        }
    }
}
