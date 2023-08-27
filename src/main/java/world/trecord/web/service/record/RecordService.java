package world.trecord.web.service.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;
import world.trecord.web.service.record.response.RecordCommentsResponse;
import world.trecord.web.service.record.response.RecordCreateResponse;
import world.trecord.web.service.record.response.RecordDeleteResponse;
import world.trecord.web.service.record.response.RecordInfoResponse;

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

        FeedEntity feedEntity = findFeedEntityBy(recordCreateRequest.getFeedId());

        checkPermissionOverFeed(userEntity, feedEntity);

        RecordEntity recordEntity = recordRepository.save(recordCreateRequest.toEntity(feedEntity));

        return RecordCreateResponse.builder()
                .writerEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }

    @Transactional
    public RecordInfoResponse updateRecord(Long userId, RecordUpdateRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = findFeedEntityBy(request.getFeedId());

        checkPermissionOverFeed(userEntity, feedEntity);

        RecordEntity recordEntity = findRecordEntityBy(request.getRecordId());

        updateRecordEntity(request, recordEntity);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(userId)
                .build();
    }

    @Transactional
    public RecordDeleteResponse deleteRecord(Long userId, Long recordId) {
        UserEntity userEntity = findUserEntityBy(userId);

        RecordEntity recordEntity = findRecordEntityWithFeedEntityAndCommentEntitiesBy(recordId);

        FeedEntity feedEntity = findFeedEntityBy(recordEntity.getFeedEntity().getId());

        checkPermissionOverFeed(userEntity, feedEntity);

        recordEntity.getCommentEntities().clear();

        recordRepository.delete(recordEntity);

        return RecordDeleteResponse.builder()
                .recordEntity(recordEntity)
                .build();
    }

    public RecordCommentsResponse getRecordComments(Long recordId, Long viewerId) {
        RecordEntity recordEntity = findRecordEntityBy(recordId);

        List<CommentEntity> commentEntities = commentRepository.findCommentEntityWithUserEntityByRecordEntityOrderByCreatedDateTimeAsc(recordEntity);

        return RecordCommentsResponse.builder()
                .commentEntities(commentEntities)
                .viewerId(viewerId)
                .build();
    }

    private RecordEntity findRecordEntityBy(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private RecordEntity findRecordEntityWithFeedEntityAndCommentEntitiesBy(Long recordId) {
        return recordRepository.findRecordEntityWithFeedEntityAndCommentEntitiesById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private FeedEntity findFeedEntityBy(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));
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