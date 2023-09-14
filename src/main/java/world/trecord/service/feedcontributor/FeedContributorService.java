package world.trecord.service.feedcontributor;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.feedcontributor.FeedContributorStatus;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEvent;
import world.trecord.exception.CustomException;
import world.trecord.service.feedcontributor.request.FeedInviteRequest;
import world.trecord.service.feedcontributor.response.UserFeedContributorListResponse;

import java.util.Objects;

import static world.trecord.domain.feedcontributor.FeedContributorStatus.EXPELLED;
import static world.trecord.domain.feedcontributor.FeedContributorStatus.LEFT;
import static world.trecord.domain.notification.enumeration.NotificationType.FEED_INVITATION;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedContributorService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final RecordRepository recordRepository;
    private final FeedContributorRepository feedContributorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void inviteUserToFeed(Long requestUserId, Long feedId, FeedInviteRequest request) {
        FeedEntity feedEntity = findFeedWithContributorsForUpdateOrException(feedId);
        ensureRequestUserIsFeedOwner(feedEntity, requestUserId);
        UserEntity invitee = findUserOrException(request.getUserToId());
        ensureNotSelfInviting(requestUserId, invitee.getId());
        ensureInviteeNotAlreadyInvited(feedEntity, invitee.getId());
        saveFeedContributor(feedEntity, invitee);

        eventPublisher.publishEvent(new NotificationEvent(invitee.getId(), requestUserId, FEED_INVITATION, buildNotificationArgs(invitee, feedEntity)));
    }

    @Transactional
    public void expelUserFromFeed(Long requestUserId, Long contributorId, Long feedId) {
        FeedEntity feedEntity = findFeedWithContributorsForUpdateOrException(feedId);
        ensureRequestUserIsFeedOwner(feedEntity, requestUserId);
        UserEntity contributor = findUserOrException(contributorId);
        ensureNotSelfExpelling(requestUserId, contributor.getId());
        ensureUserIsFeedContributor(feedEntity, contributor.getId());
        deleteFeedContributor(feedEntity, contributor.getId(), EXPELLED);
    }

    public Page<UserFeedContributorListResponse> getUserParticipatingFeeds(Long userId, Pageable pageable) {
        return feedContributorRepository.findWithFeedEntityByUserEntityId(userId, pageable)
                .map(FeedContributorEntity::getFeedEntity)
                .map(UserFeedContributorListResponse::fromEntity);
    }

    @Transactional
    public void leaveFeed(Long userId, Long feedId) {
        FeedEntity feedEntity = findFeedWithContributorsForUpdateOrException(feedId);
        ensureRequestUserIsNotFeedOwner(feedEntity, userId);
        if (!feedEntity.isContributor(userId)) {
            throw new CustomException(USER_NOT_INVITED);
        }
        deleteFeedContributor(feedEntity, userId, LEFT);
    }

    private void saveFeedContributor(FeedEntity feedEntity, UserEntity userEntity) {
        feedContributorRepository.save(FeedContributorEntity.builder()
                .feedEntity(feedEntity)
                .userEntity(userEntity)
                .build());
    }

    private void deleteFeedContributor(FeedEntity feedEntity, Long userId, FeedContributorStatus status) {
        feedEntity.removeFeedContributor(userId);
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(userId, feedEntity.getId(), status);
        recordRepository.deleteByFeedEntityIdAndUserEntityId(feedEntity.getId(), userId);
    }

    private void ensureNotSelfExpelling(Long requestUserId, Long expelledId) {
        if (Objects.equals(requestUserId, expelledId)) {
            throw new CustomException(SELF_EXPELLING_NOT_ALLOWED);
        }
    }

    private void ensureNotSelfInviting(Long requestUserId, Long inviteeId) {
        if (Objects.equals(requestUserId, inviteeId)) {
            throw new CustomException(SELF_INVITATION_NOT_ALLOWED);
        }
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private FeedEntity findFeedWithContributorsForUpdateOrException(Long feedId) {
        return feedRepository.findWithFeedContributorsByIdForUpdate(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private void ensureRequestUserIsNotFeedOwner(FeedEntity feedEntity, Long userId) {
        if (feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FEED_OWNER_LEAVING_NOT_ALLOWED);
        }
    }

    private void ensureRequestUserIsFeedOwner(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private void ensureUserIsFeedContributor(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isContributor(userId)) {
            throw new CustomException(USER_NOT_INVITED);
        }
    }

    private void ensureInviteeNotAlreadyInvited(FeedEntity feedEntity, Long userId) {
        if (feedEntity.isContributor(userId)) {
            throw new CustomException(USER_ALREADY_INVITED);
        }
    }

    private NotificationArgs buildNotificationArgs(UserEntity userFromEntity, FeedEntity feedEntity) {
        return NotificationArgs.builder()
                .feedEntity(feedEntity)
                .userFromEntity(userFromEntity)
                .build();
    }
}