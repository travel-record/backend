package world.trecord.web.service.record;

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
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordSequenceSwapRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;
import world.trecord.web.service.record.response.RecordCommentsResponse;
import world.trecord.web.service.record.response.RecordCreateResponse;
import world.trecord.web.service.record.response.RecordInfoResponse;
import world.trecord.web.service.record.response.RecordSequenceSwapResponse;

import java.util.List;

import static world.trecord.web.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final UserRecordLikeRepository userRecordLikeRepository;
    private final NotificationRepository notificationRepository;
    private final CommentRepository commentRepository;

    public RecordInfoResponse getRecord(Long viewerId, Long recordId) {
        RecordEntity recordEntity = getRecordOrException(recordId);

        boolean liked = userLiked(recordEntity, viewerId);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(viewerId)
                .liked(liked)
                .build();
    }

    @Transactional
    public RecordCreateResponse createRecord(Long userId, RecordCreateRequest recordCreateRequest) {
        FeedEntity feedEntity = getFeedOrException(recordCreateRequest.getFeedId());

        checkPermissionOverFeed(feedEntity, userId);

        int nextSequence = getNextSequence(recordCreateRequest, feedEntity);

        RecordEntity savedRecordEntity = recordRepository.save(recordCreateRequest.toEntity(feedEntity, nextSequence));

        return RecordCreateResponse.builder()
                .writerEntity(feedEntity.getUserEntity())
                .recordEntity(savedRecordEntity)
                .build();
    }

    @Transactional
    public RecordInfoResponse updateRecord(Long userId, Long recordId, RecordUpdateRequest request) {
        RecordEntity recordEntity = getRecordOrException(recordId);

        FeedEntity feedEntity = getFeedOrException(recordEntity.getFeedEntity().getId());

        checkPermissionOverFeed(feedEntity, userId);

        recordEntity.update(request.toUpdateEntity());

        recordRepository.saveAndFlush(recordEntity);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(userId)
                .build();
    }

    // TODO 로직 변경
    @Transactional
    public RecordSequenceSwapResponse swapRecordSequence(Long userId, RecordSequenceSwapRequest request) {
        RecordEntity originalRecord = getRecordOrException(request.getOriginalRecordId());

        RecordEntity targetRecord = getRecordOrException(request.getTargetRecordId());

        checkHasSameFeed(originalRecord, targetRecord);

        FeedEntity feedEntity = getFeedOrException(originalRecord.getFeedEntity().getId());

        checkPermissionOverFeed(feedEntity, userId);

        originalRecord.swapSequenceWith(targetRecord);

        recordRepository.saveAllAndFlush(List.of(originalRecord, targetRecord));

        return RecordSequenceSwapResponse.builder()
                .originalRecordId(targetRecord.getId())
                .targetRecordId(originalRecord.getId())
                .build();
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        RecordEntity recordEntity = getRecordOrException(recordId);

        FeedEntity feedEntity = getFeedOrException(recordEntity.getFeedEntity().getId());

        checkPermissionOverFeed(feedEntity, userId);

        commentRepository.deleteAllByRecordEntityId(recordId);
        userRecordLikeRepository.deleteAllByRecordEntityId(recordId);
        notificationRepository.deleteAllByRecordEntityId(recordId);

        recordRepository.softDeleteById(recordId);
    }

    public RecordCommentsResponse getRecordComments(Long recordId, Long viewerId) {
        RecordEntity recordEntity = getRecordOrException(recordId);

        List<CommentEntity> commentEntities = commentRepository.findWithUserEntityByRecordEntityIdOrderByCreatedDateTimeAsc(recordEntity.getId());

        return RecordCommentsResponse.builder()
                .commentEntities(commentEntities)
                .viewerId(viewerId)
                .build();
    }

    private FeedEntity getFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private int getNextSequence(RecordCreateRequest recordCreateRequest, FeedEntity feedEntity) {
        // TODO 동시성 처리
        return recordRepository.findMaxSequenceByFeedEntityIdAndDate(feedEntity.getId(), recordCreateRequest.getDate()).orElse(0) + 1;
    }

    private void checkHasSameFeed(RecordEntity originalRecord, RecordEntity targetRecord) {
        if (!originalRecord.hasSameFeed(targetRecord)) {
            throw new CustomException(INVALID_ARGUMENT);
        }
    }

    private RecordEntity getRecordOrException(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private void checkPermissionOverFeed(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isManagedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private boolean userLiked(RecordEntity recordEntity, Long viewerId) {
        if (viewerId == null) {
            return false;
        }

        return userRepository.findById(viewerId)
                .map(userEntity -> userRecordLikeRepository.existsByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId()))
                .orElseGet(() -> false);
    }
}