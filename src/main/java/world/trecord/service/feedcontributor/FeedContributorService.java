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
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEvent;
import world.trecord.exception.CustomException;
import world.trecord.service.feedcontributor.request.FeedExpelRequest;
import world.trecord.service.feedcontributor.request.FeedInviteRequest;
import world.trecord.service.feedcontributor.response.UserFeedContributorListResponse;

import java.util.Objects;

import static world.trecord.domain.feedcontributor.FeedContributorStatus.EXPELLED;
import static world.trecord.domain.notification.enumeration.NotificationType.FEED_INVITATION;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedContributorService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FeedContributorRepository feedContributorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void inviteUser(Long userFromId, Long feedId, FeedInviteRequest request) {
        FeedEntity feedEntity = findFeedWithContributorsForUpdateOrException(feedId);
        ensureUserIsFeedOwner(feedEntity, userFromId);
        UserEntity invitedUser = findUserOrException(request.getUserToId());
        ensureNotSelfInviting(userFromId, invitedUser.getId());
        ensureUserNotAlreadyInvited(feedEntity, invitedUser.getId());
        saveFeedContributor(feedEntity, invitedUser);

        eventPublisher.publishEvent(new NotificationEvent(invitedUser.getId(), userFromId, FEED_INVITATION, buildNotificationArgs(invitedUser, feedEntity)));
    }

    @Transactional
    public void expelUser(Long userFromId, Long feedId, FeedExpelRequest request) {
        FeedEntity feedEntity = findFeedWithContributorsForUpdateOrException(feedId);
        ensureUserIsFeedOwner(feedEntity, userFromId);
        UserEntity expelledUser = findUserOrException(request.getUserToId());
        ensureNotSelfExpelling(userFromId, expelledUser.getId());
        ensureUserIsFeedContributor(feedEntity, expelledUser.getId());
        deleteFeedContributor(feedEntity, expelledUser.getId());
    }

    public Page<UserFeedContributorListResponse> getUserParticipatingFeeds(Long userId, Pageable pageable) {
        return feedContributorRepository.findWithFeedEntityByUserEntityId(userId, pageable)
                .map(FeedContributorEntity::getFeedEntity)
                .map(UserFeedContributorListResponse::fromEntity);
    }

    private void deleteFeedContributor(FeedEntity feedEntity, Long userToId) {
        feedEntity.removeFeedContributor(userToId);
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(userToId, feedEntity.getId(), EXPELLED);
    }

    private void ensureNotSelfExpelling(Long userFromId, Long userToId) {
        if (Objects.equals(userFromId, userToId)) {
            throw new CustomException(SELF_EXPELLING_NOT_ALLOWED);
        }
    }

    private void saveFeedContributor(FeedEntity feedEntity, UserEntity userToEntity) {
        feedContributorRepository.save(FeedContributorEntity.builder()
                .feedEntity(feedEntity)
                .userEntity(userToEntity)
                .build());
    }

    private void ensureNotSelfInviting(Long userFromId, Long userToId) {
        if (Objects.equals(userFromId, userToId)) {
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

    private void ensureUserIsFeedOwner(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private void ensureUserIsFeedContributor(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isContributor(userId)) {
            throw new CustomException(USER_NOT_INVITED);
        }
    }

    private void ensureUserNotAlreadyInvited(FeedEntity feedEntity, Long userId) {
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