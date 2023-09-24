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
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.feed.request.FeedCreateRequest;
import world.trecord.dto.feed.request.FeedUpdateRequest;
import world.trecord.dto.feed.response.FeedCreateResponse;
import world.trecord.dto.feed.response.FeedInfoResponse;
import world.trecord.dto.feed.response.FeedListResponse;
import world.trecord.dto.feed.response.FeedRecordsResponse;
import world.trecord.exception.CustomException;

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

    // TODO pagination
    public FeedListResponse getFeedList(Long userId) {
        return FeedListResponse.of(feedRepository.findByUserEntityIdOrderByStartAtDesc(userId));
    }

    // TODO feed contributors 같이 추가
    public FeedInfoResponse getFeed(Optional<Long> viewerId, Long feedId) {
        FeedEntity feedEntity = findFeedOrException(feedId);
        return FeedInfoResponse.of(feedEntity, viewerId.orElse(null));
    }

    public Page<FeedRecordsResponse> getFeedRecords(Long feedId, Pageable pageable) {
        FeedEntity feedEntity = findFeedOrException(feedId);
        return recordRepository.findRecordListByFeedEntityId(feedId, pageable)
                .map(it -> FeedRecordsResponse.of(it, feedEntity.getStartAt()));
    }

    @Transactional
    public FeedCreateResponse createFeed(Long userId, FeedCreateRequest request) {
        UserEntity userEntity = findUserOrException(userId);
        FeedEntity feedEntity = feedRepository.save(request.toEntity(userEntity));
        return FeedCreateResponse.of(feedEntity);
    }

    @Transactional
    public void updateFeed(Long userId, Long feedId, FeedUpdateRequest request) {
        FeedEntity feedEntity = findFeedOrException(feedId);
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

    private void ensureUserIsFeedOwner(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }
}
