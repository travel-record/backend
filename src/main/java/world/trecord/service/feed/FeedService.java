package world.trecord.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.record.projection.RecordWithFeedProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.service.feed.request.FeedCreateRequest;
import world.trecord.service.feed.request.FeedUpdateRequest;
import world.trecord.service.feed.response.FeedCreateResponse;
import world.trecord.service.feed.response.FeedInfoResponse;
import world.trecord.service.feed.response.FeedListResponse;

import java.util.List;
import java.util.Optional;

import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;
    private final RecordSequenceRepository recordSequenceRepository;

    // TODO pageable
    public FeedListResponse getFeedList(Long userId) {
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userId);

        return FeedListResponse.builder()
                .feedEntities(feedEntities)
                .build();
    }

    public FeedInfoResponse getFeed(Optional<Long> viewerId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordsByFeedEntityId(feedId);

        return FeedInfoResponse.builder()
                .feedEntity(feedEntity)
                .viewerId(viewerId.orElse(null))
                .projectionList(projectionList)
                .build();
    }

    @Transactional
    public FeedCreateResponse createFeed(Long userId, FeedCreateRequest request) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        FeedEntity feedEntity = feedRepository.save(request.toEntity(userEntity));

        return FeedCreateResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public void updateFeed(Long userId, Long feedId, FeedUpdateRequest request) {
        FeedEntity feedEntity = findFeedForUpdateOrException(feedId);

        ensureUserHasPermissionOverFeed(feedEntity, userId);

        feedEntity.update(request.toUpdateEntity());
        feedRepository.saveAndFlush(feedEntity);
    }

    @Transactional
    public void deleteFeed(Long userId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);

        ensureUserHasPermissionOverFeed(feedEntity, userId);

        recordRepository.deleteAllByFeedEntityId(feedId);
        recordSequenceRepository.deleteAllByFeedEntityId(feedId);
        feedRepository.delete(feedEntity);
    }

    private void ensureUserHasPermissionOverFeed(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isManagedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private FeedEntity findFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private FeedEntity findFeedForUpdateOrException(Long feedId) {
        return feedRepository.findByIdForUpdate(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }
}
