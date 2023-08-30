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
import world.trecord.domain.users.UserEntity;
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

    public RecordInfoResponse getRecordInfo(Long recordId, Long viewerId) {
        RecordEntity recordEntity = findRecordEntityBy(recordId);

        boolean liked = hasUserLikedRecord(viewerId, recordEntity);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(viewerId)
                .liked(liked)
                .build();
    }

    @Transactional
    public RecordCreateResponse createRecord(Long userId, RecordCreateRequest recordCreateRequest) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(recordCreateRequest.getFeedId());

        checkPermissionOverFeed(userEntity, feedEntity);

        // TODO 동시성 처리
        int nextSequence = getNextSequence(recordCreateRequest, feedEntity);

        RecordEntity savedRecordEntity = recordRepository.save(recordCreateRequest.toEntity(feedEntity, nextSequence));

        return RecordCreateResponse.builder()
                .writerEntity(userEntity)
                .recordEntity(savedRecordEntity)
                .build();
    }

    @Transactional
    public RecordInfoResponse updateRecord(Long userId, Long recordId, RecordUpdateRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        RecordEntity recordEntity = findRecordEntityBy(recordId);

        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(recordEntity.getFeedEntity().getId());

        checkPermissionOverFeed(userEntity, feedEntity);

        updateRecordEntity(request, recordEntity);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(userId)
                .build();
    }

    @Transactional
    public RecordSequenceSwapResponse swapRecordSequence(Long userId, RecordSequenceSwapRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        RecordEntity originalRecord = findRecordEntityBy(request.getOriginalRecordId());

        RecordEntity targetRecord = findRecordEntityBy(request.getTargetRecordId());

        checkHasSameFeed(originalRecord, targetRecord);

        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(originalRecord.getFeedEntity().getId());

        checkPermissionOverFeed(userEntity, feedEntity);

        originalRecord.swapSequenceWith(targetRecord);

        return RecordSequenceSwapResponse.builder()
                .originalRecordId(targetRecord.getId())
                .targetRecordId(originalRecord.getId())
                .build();
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        UserEntity userEntity = findUserEntityBy(userId);

        RecordEntity recordEntity = findRecordEntityBy(recordId);

        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(recordEntity.getFeedEntity().getId());

        checkPermissionOverFeed(userEntity, feedEntity);

        commentRepository.deleteAllByRecordEntity(recordEntity);
        userRecordLikeRepository.deleteAllByRecordEntity(recordEntity);
        notificationRepository.deleteAllByRecordEntity(recordEntity);
        recordRepository.softDelete(recordEntity);
    }

    public RecordCommentsResponse getRecordComments(Long recordId, Long viewerId) {
        RecordEntity recordEntity = findRecordEntityBy(recordId);

        List<CommentEntity> commentEntities = commentRepository.findCommentEntityWithUserEntityByRecordEntityOrderByCreatedDateTimeAsc(recordEntity);

        return RecordCommentsResponse.builder()
                .commentEntities(commentEntities)
                .viewerId(viewerId)
                .build();
    }

    private int getNextSequence(RecordCreateRequest recordCreateRequest, FeedEntity feedEntity) {
        return recordRepository.findMaxSequenceByFeedAndDate(feedEntity.getId(), recordCreateRequest.getDate()).orElse(0) + 1;
    }

    private void checkHasSameFeed(RecordEntity originalRecord, RecordEntity targetRecord) {
        if (!originalRecord.hasSameFeed(targetRecord)) {
            throw new CustomException(INVALID_ARGUMENT);
        }
    }

    private RecordEntity findRecordEntityBy(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private FeedEntity findFeedEntityWithUserEntityBy(Long feedId) {
        return feedRepository.findFeedEntityWithUserEntityById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private void updateRecordEntity(RecordUpdateRequest request, RecordEntity recordEntity) {
        recordEntity.update(request.toUpdateEntity());
    }

    private void checkPermissionOverFeed(UserEntity userEntity, FeedEntity feedEntity) {
        if (!userEntity.isManagerOf(feedEntity)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private boolean hasUserLikedRecord(Long viewerId, RecordEntity recordEntity) {
        if (viewerId == null) {
            return false;
        }

        return userRepository.findById(viewerId)
                .map(userEntity -> userRecordLikeRepository.existsByUserEntityAndRecordEntity(userEntity, recordEntity))
                .orElseGet(() -> false);
    }
}