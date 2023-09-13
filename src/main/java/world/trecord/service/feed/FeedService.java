package world.trecord.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.notification.NotificationRepository;
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
import world.trecord.service.feed.response.FeedRecordsResponse;

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
    private final FeedContributorRepository feedContributorRepository;
    private final NotificationRepository notificationRepository;
    private final RecordSequenceRepository recordSequenceRepository;

    public FeedListResponse getFeedList(Long userId) {
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userId);

        return FeedListResponse.builder()
                .feedEntities(feedEntities)
                .build();
    }

    // TODO
    // records와 분리
    // feed contributors 같이 추가
    public FeedInfoResponse getFeed(Optional<Long> viewerId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);
        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordsByFeedEntityId(feedId);

        return FeedInfoResponse.builder()
                .feedEntity(feedEntity)
                .viewerId(viewerId.orElse(null))
                .projectionList(projectionList)
                .build();
    }

    public Page<FeedRecordsResponse> getFeedRecords(Long feedId, Pageable pageable) {
        FeedEntity feedEntity = findFeedOrException(feedId);
        return recordRepository.findRecordListByFeedEntityId(feedId, pageable)
                .map(it -> FeedRecordsResponse.builder()
                        .projection(it)
                        .feedStartAt(feedEntity.getStartAt())
                        .build());
    }

    @Transactional
    public FeedCreateResponse createFeed(Long userId, FeedCreateRequest request) {
        UserEntity userEntity = findUserOrException(userId);
        FeedEntity feedEntity = feedRepository.save(request.toEntity(userEntity));

        return FeedCreateResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public void updateFeed(Long userId, Long feedId, FeedUpdateRequest request) {
        FeedEntity feedEntity = findFeedForUpdateOrException(feedId);

        ensureUserIsFeedOwner(feedEntity, userId);

        feedEntity.update(request.toUpdateEntity());
        feedRepository.saveAndFlush(feedEntity);
    }

    @Transactional
    public void deleteFeed(Long userId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);

        ensureUserIsFeedOwner(feedEntity, userId);

        notificationRepository.deleteAllByFeedEntityId(feedId);
        feedContributorRepository.deleteAllByFeedEntityId(feedId);
        recordRepository.deleteAllByFeedEntityId(feedId);
        recordSequenceRepository.deleteAllByFeedEntityId(feedId);
        feedRepository.delete(feedEntity);
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private FeedEntity findFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private FeedEntity findFeedForUpdateOrException(Long feedId) {
        return feedRepository.findByIdForUpdate(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private void ensureUserIsFeedOwner(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }
}
