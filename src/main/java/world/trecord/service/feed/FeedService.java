package world.trecord.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.projection.RecordWithFeedProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.service.feed.response.FeedCreateResponse;
import world.trecord.service.feed.response.FeedListResponse;
import world.trecord.service.feed.response.FeedUpdateResponse;
import world.trecord.exception.CustomException;
import world.trecord.service.feed.request.FeedCreateRequest;
import world.trecord.service.feed.request.FeedUpdateRequest;
import world.trecord.service.feed.response.FeedInfoResponse;

import java.util.List;

import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;

    // TODO pageable
    public FeedListResponse getFeedList(Long userId) {
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userId);

        return FeedListResponse.builder()
                .feedEntities(feedEntities)
                .build();
    }

    public FeedInfoResponse getFeed(Long viewerId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordsByFeedEntityId(feedId);

        return FeedInfoResponse.builder()
                .feedEntity(feedEntity)
                .viewerId(viewerId)
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
    public FeedUpdateResponse updateFeed(Long userId, Long feedId, FeedUpdateRequest request) {
        FeedEntity feedEntity = findFeedOrException(feedId);

        doCheckPermissionOverFeed(feedEntity, userId);

        feedEntity.update(request.toUpdateEntity());

        feedRepository.saveAndFlush(feedEntity);

        return FeedUpdateResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public void deleteFeed(Long userId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);

        doCheckPermissionOverFeed(feedEntity, userId);

        recordRepository.deleteAllByFeedEntityId(feedId);

        feedRepository.softDeleteById(feedId);
    }

    private void doCheckPermissionOverFeed(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isManagedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private FeedEntity findFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }
}
