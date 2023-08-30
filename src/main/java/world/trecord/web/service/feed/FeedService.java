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
import world.trecord.web.service.feed.response.*;

import java.util.List;

import static world.trecord.web.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;

    public FeedListResponse getFeedListBy(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        List<FeedEntity> feedEntities = feedRepository.findByUserEntityOrderByStartAtDesc(userEntity);

        return FeedListResponse.builder()
                .feedEntities(feedEntities)
                .build();
    }

    public FeedInfoResponse getFeedBy(Long feedId, Long viewerId) {
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
        UserEntity userEntity = findUserEntityBy(userId);

        // TODO find feedEntity with record entity specific field
        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(feedId);

        checkPermissionOverFeed(userEntity, feedEntity);

        updateFeedEntity(request, feedEntity);

        return FeedUpdateResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public FeedDeleteResponse deleteFeed(Long userId, Long feedId) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = findFeedEntityWithUserEntityBy(feedId);

        checkPermissionOverFeed(userEntity, feedEntity);

        recordRepository.deleteAllByFeedEntity(feedEntity);
        feedRepository.softDelete(feedEntity);

        return FeedDeleteResponse.builder()
                .feedEntity(feedEntity)
                .build();
    }

    private void checkPermissionOverFeed(UserEntity userEntity, FeedEntity feedEntity) {
        if (!userEntity.isManagerOf(feedEntity)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private FeedEntity findFeedEntityWithUserEntityBy(Long feedId) {
        return feedRepository.findFeedEntityWithUserEntityById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private void updateFeedEntity(FeedUpdateRequest request, FeedEntity feedEntity) {
        feedEntity.update(request.toUpdateEntity());
    }
}
