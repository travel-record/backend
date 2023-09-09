package world.trecord.service.invitation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.invitation.InvitationEntity;
import world.trecord.domain.invitation.InvitationRepository;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEvent;
import world.trecord.exception.CustomException;
import world.trecord.service.invitation.request.FeedInviteRequest;

import java.util.Objects;

import static world.trecord.domain.notification.enumeration.NotificationType.FEED_INVITATION;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class InvitationService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final InvitationRepository invitationRepository;
    private final FeedContributorRepository feedContributorRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void inviteUser(Long userFromId, Long feedId, FeedInviteRequest request) {
        FeedEntity feedEntity = findFeedForUpdateOrException(feedId);

        ensureUserIsFeedOwner(feedEntity, userFromId);

        UserEntity userToEntity = findUserOrException(request.getUserToId());

        ensureNotSelfInviting(userFromId, userToEntity.getId());

        if (feedContributorRepository.existsByUserEntityIdAndFeedEntityId(userToEntity.getId(), feedEntity.getId())) {
            throw new CustomException(USER_ALREADY_INVITED);
        }

        saveInvitation(feedEntity, userToEntity);

        saveManager(feedEntity, userToEntity);

        eventPublisher.publishEvent(new NotificationEvent(userToEntity.getId(), userFromId, FEED_INVITATION, buildNotificationArgs(userToEntity, feedEntity)));
    }

    private void saveInvitation(FeedEntity feedEntity, UserEntity userToEntity) {
        invitationRepository.save(InvitationEntity.builder()
                .userToEntity(userToEntity)
                .feedEntity(feedEntity)
                .build());
    }

    private void saveManager(FeedEntity feedEntity, UserEntity userToEntity) {
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

    private FeedEntity findFeedForUpdateOrException(Long feedId) {
        return feedRepository.findByIdForUpdate(feedId).
                orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private void ensureUserIsFeedOwner(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isOwnedBy(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private NotificationArgs buildNotificationArgs(UserEntity usrFromEntity, FeedEntity feedEntity) {
        return NotificationArgs.builder()
                .feedEntity(feedEntity)
                .userFromEntity(usrFromEntity)
                .build();
    }
}