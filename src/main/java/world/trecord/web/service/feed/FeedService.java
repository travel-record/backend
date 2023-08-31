package world.trecord.web.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.projection.RecordWithFeedProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;
import world.trecord.web.service.feed.response.FeedCreateResponse;
import world.trecord.web.service.feed.response.FeedInfoResponse;
import world.trecord.web.service.feed.response.FeedListResponse;
import world.trecord.web.service.feed.response.FeedUpdateResponse;

import java.util.List;
import java.util.Objects;

import static world.trecord.web.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;

    public FeedListResponse getFeedList(Long userId) {
        List<FeedEntity> feedEntities = feedRepository.findByUserEntityIdOrderByStartAtDesc(userId);

        return FeedListResponse.builder()
                .feedEntities(feedEntities)
                .build();
    }

    public FeedInfoResponse getFeed(Long viewerId, Long feedId) {
        FeedEntity feedEntity = feedRepository.findFeedEntityWithUserEntityById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));

        List<RecordWithFeedProjection> projectionList = recordRepository.findRecordEntityByFeedId(feedId);

        return FeedInfoResponse.builder()
                .feedEntity(feedEntity)
                .viewerId(viewerId)
                .projectionList(projectionList)
                .build();
    }

    @Transactional
    public FeedCreateResponse createFeed(Long userId, FeedCreateRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = feedRepository.save(request.toEntity(userEntity));

        return FeedCreateResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public FeedUpdateResponse updateFeed(Long userId, Long feedId, FeedUpdateRequest request) {
        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(feedId);

        checkPermissionOverFeed(feedEntity, userId);

        feedEntity.update(request.toUpdateEntity());

        return FeedUpdateResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public void deleteFeed(Long userId, Long feedId) {
        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(feedId);

        checkPermissionOverFeed(feedEntity, userId);

        recordRepository.deleteAllByFeedEntity(feedEntity);

        feedRepository.softDelete(feedEntity);
    }

    private void checkPermissionOverFeed(FeedEntity feedEntity, Long userId) {
        if (!Objects.equals(feedEntity.getUserEntity().getId(), userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private FeedEntity findFeedEntityWithUserEntityBy(Long feedId) {
        return feedRepository.findFeedEntityWithUserEntityById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

}
