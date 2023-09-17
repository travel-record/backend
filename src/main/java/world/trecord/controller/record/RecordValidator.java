package world.trecord.controller.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordUpdateRequest;
import world.trecord.exception.CustomException;

import java.util.Objects;

import static world.trecord.exception.CustomExceptionError.FEED_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.RECORD_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Component
public class RecordValidator {
    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;

    public void verify(RecordCreateRequest request) throws BindException {
        FeedEntity feedEntity = feedRepository.findById(request.getFeedId()).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));

        String recordCreateRequest = "recordCreateRequest";
        String fieldDate = "date";

        if (Objects.nonNull(feedEntity.getStartAt()) && request.getDate().isBefore(feedEntity.getStartAt())) {
            BindException bindException = new BindException(request, recordCreateRequest);
            bindException.addError(new FieldError(recordCreateRequest, fieldDate, "Date should be after feed start time."));
            throw bindException;
        }

        if (Objects.nonNull(feedEntity.getEndAt()) && request.getDate().isAfter(feedEntity.getEndAt())) {
            BindException bindException = new BindException(request, recordCreateRequest);
            bindException.addError(new FieldError(recordCreateRequest, fieldDate, "Date should be before feed end time."));
            throw bindException;
        }
    }

    public void verify(Long recordId, RecordUpdateRequest request) throws BindException {
        RecordEntity recordEntity = recordRepository.findWithFeedEntityById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
        FeedEntity feedEntity = recordEntity.getFeedEntity();

        String recordUpdateRequest = "recordUpdateRequest";
        String fieldDate = "date";

        if (Objects.nonNull(feedEntity.getStartAt()) && request.getDate().isBefore(feedEntity.getStartAt())) {
            BindException bindException = new BindException(request, recordUpdateRequest);
            bindException.addError(new FieldError(recordUpdateRequest, fieldDate, "Date should be after start end time."));
            throw bindException;
        }

        if (Objects.nonNull(feedEntity.getEndAt()) && request.getDate().isAfter(feedEntity.getEndAt())) {
            BindException bindException = new BindException(request, recordUpdateRequest);
            bindException.addError(new FieldError(recordUpdateRequest, fieldDate, "Date should be before feed end time."));
            throw bindException;
        }
    }
}
