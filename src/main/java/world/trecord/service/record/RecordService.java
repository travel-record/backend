package world.trecord.service.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceEntity;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.exception.CustomException;
import world.trecord.service.record.request.RecordCreateRequest;
import world.trecord.service.record.request.RecordSequenceSwapRequest;
import world.trecord.service.record.request.RecordUpdateRequest;
import world.trecord.service.record.response.RecordCommentsResponse;
import world.trecord.service.record.response.RecordCreateResponse;
import world.trecord.service.record.response.RecordInfoResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final FeedRepository feedRepository;
    private final UserRecordLikeRepository userRecordLikeRepository;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final RecordSequenceRepository recordSequenceRepository;

    public RecordInfoResponse getRecord(Optional<Long> viewerId, Long recordId) {
        RecordEntity recordEntity = findRecordOrException(recordId);
        boolean liked = userLiked(recordEntity, viewerId);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(viewerId.orElse(null))
                .liked(liked)
                .build();
    }

    @Transactional
    public RecordCreateResponse createRecord(Long userId, RecordCreateRequest request) {
        FeedEntity feedEntity = findFeedOrException(request.getFeedId());

        ensureUserHasPermissionOverRecord(feedEntity, userId);

        int nextSequence = findNextSequence(feedEntity.getId(), request.getDate());
        RecordEntity savedRecordEntity = recordRepository.save(request.toEntity(feedEntity, nextSequence));

        return RecordCreateResponse.builder()
                .writerEntity(feedEntity.getUserEntity())
                .recordEntity(savedRecordEntity)
                .build();
    }

    @Transactional
    public void updateRecord(Long userId, Long recordId, RecordUpdateRequest request) {
        RecordEntity recordEntity = findRecordForUpdateOrException(recordId);
        FeedEntity feedEntity = findFeedOrException(recordEntity.getFeedEntity().getId());

        ensureUserHasPermissionOverRecord(feedEntity, userId);

        recordEntity.update(request.toUpdateEntity());
        recordRepository.saveAndFlush(recordEntity);
    }

    @Transactional
    public void swapRecordSequence(Long userId, RecordSequenceSwapRequest request) {
        RecordEntity originalRecord = findRecordForUpdateOrException(request.getOriginalRecordId());
        RecordEntity targetRecord = findRecordForUpdateOrException(request.getTargetRecordId());

        ensureRecordsHasSameFeed(originalRecord, targetRecord);

        FeedEntity feedEntity = findFeedOrException(originalRecord.getFeedEntity().getId());

        ensureUserHasPermissionOverRecord(feedEntity, userId);

        originalRecord.swapSequenceWith(targetRecord);
        recordRepository.saveAllAndFlush(List.of(originalRecord, targetRecord));
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        RecordEntity recordEntity = findRecordForUpdateOrException(recordId);
        FeedEntity feedEntity = findFeedOrException(recordEntity.getFeedEntity().getId());

        ensureUserHasPermissionOverRecord(feedEntity, userId);

        commentRepository.deleteAllByRecordEntityId(recordId);
        userRecordLikeRepository.deleteAllByRecordEntityId(recordId);
        notificationRepository.deleteAllByRecordEntityId(recordId);
        recordRepository.delete(recordEntity);
    }

    public RecordCommentsResponse getRecordComments(Optional<Long> viewerId, Long recordId) {
        RecordEntity recordEntity = findRecordOrException(recordId);
        List<CommentEntity> commentEntities = commentRepository.findWithUserEntityByRecordEntityIdOrderByCreatedDateTimeAsc(recordEntity.getId());

        return RecordCommentsResponse.builder()
                .commentEntities(commentEntities)
                .viewerId(viewerId.orElse(null))
                .build();
    }

    private FeedEntity findFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private int findNextSequence(Long feedId, LocalDateTime date) {
        recordSequenceRepository.insertOrIncrement(feedId, date);

        RecordSequenceEntity recordSequenceEntity = recordSequenceRepository.findByFeedEntityIdAndDate(feedId, date)
                .orElseThrow(() -> new IllegalStateException("RecordSequence should exist after increment or insert"));

        return recordSequenceEntity.getSequence();
    }

    private void ensureRecordsHasSameFeed(RecordEntity originalRecord, RecordEntity targetRecord) {
        if (!originalRecord.hasSameFeed(targetRecord)) {
            throw new CustomException(INVALID_ARGUMENT);
        }
    }

    private RecordEntity findRecordOrException(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private RecordEntity findRecordForUpdateOrException(Long recordId) {
        return recordRepository.findByIdForUpdate(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private void ensureUserHasPermissionOverRecord(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private boolean userLiked(RecordEntity recordEntity, Optional<Long> viewerId) {
        return viewerId
                .filter(userId -> userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userId, recordEntity.getId()))
                .isPresent();
    }
}