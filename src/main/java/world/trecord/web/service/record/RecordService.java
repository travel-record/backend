package world.trecord.web.service.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordDeleteRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;
import world.trecord.web.service.record.response.RecordCreateResponse;
import world.trecord.web.service.record.response.RecordDeleteResponse;
import world.trecord.web.service.record.response.RecordInfoResponse;

import static world.trecord.web.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RecordService {
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;

    public RecordInfoResponse getRecordInfoBy(Long recordId, Long viewerId) {
        RecordEntity recordEntity = findRecordEntityWithFeedEntityAndCommentEntitiesBy(recordId);

        return RecordInfoResponse.builder()
                .recordEntity(recordEntity)
                .viewerId(viewerId)
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
    public RecordDeleteResponse deleteRecord(Long userId, RecordDeleteRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = findFeedEntityBy(request.getFeedId());

        checkPermissionOverFeed(userEntity, feedEntity);

        RecordEntity recordEntity = findRecordEntityWithCommentEntitiesBy(request.getRecordId());

        recordEntity.getCommentEntities().clear();

        recordRepository.delete(recordEntity);

        return RecordDeleteResponse.builder()
                .recordEntity(recordEntity)
                .build();
    }

    private RecordEntity findRecordEntityBy(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private RecordEntity findRecordEntityWithFeedEntityAndCommentEntitiesBy(Long recordId) {
        return recordRepository.findRecordEntityWithFeedEntityAndCommentEntitiesBy(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private RecordEntity findRecordEntityWithCommentEntitiesBy(Long recordId) {
        return recordRepository.findRecordEntityWithCommentEntitiesById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
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
}
