package world.trecord.web.service.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;
import world.trecord.web.service.feed.response.FeedCreateResponse;
import world.trecord.web.service.feed.response.FeedDeleteResponse;
import world.trecord.web.service.feed.response.FeedListResponse;
import world.trecord.web.service.feed.response.FeedOneResponse;

import java.util.List;

import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;

    public FeedListResponse getFeedListBy(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        List<FeedListResponse.Feed> feeds = feedRepository.findByUserEntityOrderByStartAtDesc(userEntity)
                .stream()
                .map(FeedListResponse.Feed::new).toList();

        return FeedListResponse.builder()
                .feeds(feeds)
                .build();
    }

    public FeedOneResponse getFeedBy(Long feedId) {
        FeedEntity feedEntity = findFeedEntityWithRecordEntitiesBy(feedId);

        return FeedOneResponse
                .builder()
                .feedEntity(feedEntity)
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
    public FeedOneResponse updateFeed(Long userId, FeedUpdateRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = findFeedEntityBy(request.getId());

        checkPermissionOverFeed(userEntity, feedEntity);

        updateFeedEntity(request, feedEntity);

        return FeedOneResponse
                .builder()
                .feedEntity(feedEntity)
                .build();
    }

    @Transactional
    public FeedDeleteResponse deleteFeed(Long userId, Long feedId) {
        UserEntity userEntity = findUserEntityBy(userId);

        FeedEntity feedEntity = findFeedEntityBy(feedId);

        checkPermissionOverFeed(userEntity, feedEntity);

        feedRepository.delete(feedEntity);

        return FeedDeleteResponse.builder()
                .id(feedId)
                .build();
    }

    private void checkPermissionOverFeed(UserEntity userEntity, FeedEntity feedEntity) {
        if (!userEntity.equals(feedEntity.getUserEntity()))
            throw new CustomException(FORBIDDEN);
    }

    private FeedEntity findFeedEntityBy(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));
    }

    private FeedEntity findFeedEntityWithRecordEntitiesBy(Long feedId) {
        return feedRepository.findFeedEntityWithRecordEntitiesById(feedId).orElseThrow(() -> new CustomException(NOT_EXISTING_FEED));
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private void updateFeedEntity(FeedUpdateRequest request, FeedEntity feedEntity) {
        feedEntity.update(request.getName(), request.getImageUrl(), request.getDescription()
                , request.getStartAt(), request.getEndAt(), request.getCompanion(), request.getPlace()
                , request.getSatisfaction());
    }
}
