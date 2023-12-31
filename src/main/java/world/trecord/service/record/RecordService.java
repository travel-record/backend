package world.trecord.service.record;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceEntity;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordSequenceSwapRequest;
import world.trecord.dto.record.request.RecordUpdateRequest;
import world.trecord.dto.record.response.RecordCommentResponse;
import world.trecord.dto.record.response.RecordCreateResponse;
import world.trecord.dto.record.response.RecordInfoResponse;
import world.trecord.exception.CustomException;
import world.trecord.service.feed.FeedService;
import world.trecord.service.users.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RecordService {

    private final UserService userService;
    private final FeedService feedService;
    private final RecordRepository recordRepository;
    private final RecordSequenceRepository recordSequenceRepository;
    private final UserRecordLikeRepository userRecordLikeRepository;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;
    private final FeedContributorRepository feedContributorRepository;

    public RecordInfoResponse getRecord(Long userId, Long recordId) {
        RecordEntity recordEntity = findRecordWithUserOrException(recordId);
        boolean liked = hasUserLikedRecord(recordEntity, userId);
        return RecordInfoResponse.of(recordEntity, userId, liked);
    }

    @Transactional
    public RecordCreateResponse createRecord(Long userId, RecordCreateRequest request) {
        UserEntity userEntity = userService.findUserOrException(userId);
        FeedEntity feedEntity = feedService.findFeedOrException(request.getFeedId());
        ensureUserHasWritePermissionOverRecord(userId, feedEntity);
        int recordSequence = findNextSequence(feedEntity.getId(), request.getDate());
        RecordEntity recordEntity = recordRepository.save(request.toEntity(userEntity, feedEntity, recordSequence));
        return RecordCreateResponse.of(recordEntity);
    }

    @Transactional
    public void updateRecord(Long userId, Long recordId, RecordUpdateRequest request) {
        RecordEntity recordEntity = findRecordOrException(recordId);
        ensureUserHasPermissionOverRecord(recordEntity, userId);
        recordEntity.update(request.toUpdateEntity());
        recordRepository.saveAndFlush(recordEntity);
    }

    @Transactional
    public void swapRecordSequence(Long userId, RecordSequenceSwapRequest request) {
        List<Long> recordIds = Arrays.asList(request.getOriginalRecordId(), request.getTargetRecordId());
        List<RecordEntity> recordEntityList = recordRepository.findByIdsForUpdate(recordIds);
        ensureAllRecordsFound(recordEntityList, recordIds);
        RecordEntity originalRecord = recordEntityList.get(0);
        RecordEntity targetRecord = recordEntityList.get(1);
        ensureRecordsHasSameFeed(originalRecord, targetRecord);
        FeedEntity feedEntity = feedService.findFeedOrException(originalRecord.getFeedId());
        ensureUserHasPermissionOverFeed(userId, feedEntity);

        originalRecord.swapSequenceWith(targetRecord);
        recordRepository.saveAllAndFlush(List.of(originalRecord, targetRecord));
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        RecordEntity recordEntity = findRecordOrException(recordId);
        ensureUserHasPermissionOverRecord(recordEntity, userId);

        commentRepository.deleteAllByRecordEntityId(recordId);
        userRecordLikeRepository.deleteAllByRecordEntityId(recordId);
        notificationRepository.deleteAllByRecordEntityId(recordId);
        recordRepository.delete(recordEntity);
    }

    public Page<RecordCommentResponse> getRecordComments(Long userId, Long recordId, Pageable pageable) {
        RecordEntity recordEntity = findRecordOrException(recordId);
        Page<CommentEntity> commentEntities = commentRepository.findWithCommenterAndRepliesByRecordId(recordEntity.getId(), pageable);
        return commentEntities.map(it -> RecordCommentResponse.of(it, userId));
    }

    public RecordEntity findRecordOrException(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private void ensureAllRecordsFound(List<RecordEntity> recordEntityList, List<Long> recordIds) {
        if (recordEntityList.size() != recordIds.size()) {
            throw new CustomException(RECORD_NOT_FOUND);
        }
    }

    public RecordEntity findRecordWithUserOrException(Long recordId) {
        return recordRepository.findWithUserById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    public RecordEntity findRecordWithLockOrException(Long recordId) {
        return recordRepository.findForUpdateById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private void ensureUserHasWritePermissionOverRecord(Long userId, FeedEntity feedEntity) {
        if (feedEntity.isOwnedBy(userId)) {
            return;
        }

        boolean hasWritePermission = feedContributorRepository
                .findByUserEntityIdAndFeedEntityId(userId, feedEntity.getId())
                .map(it -> it.getPermission().getRecord().getWrite())
                .orElse(false);

        if (!hasWritePermission) {
            throw new CustomException(FORBIDDEN);
        }
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

    private void ensureUserHasPermissionOverFeed(Long userId, FeedEntity feedEntity) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private void ensureUserHasPermissionOverRecord(RecordEntity recordEntity, Long userId) {
        if (!recordEntity.isUpdatable(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private boolean hasUserLikedRecord(RecordEntity recordEntity, Long viewerId) {
        Optional<Long> viewerIdOpt = Optional.ofNullable(viewerId);
        return viewerIdOpt
                .filter(userId -> userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userId, recordEntity.getId()))
                .isPresent();
    }
}