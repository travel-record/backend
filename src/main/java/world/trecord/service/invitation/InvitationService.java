package world.trecord.service.invitation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.invitation.InvitationEntity;
import world.trecord.domain.invitation.InvitationRepository;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.notification.NotificationEvent;
import world.trecord.exception.CustomException;
import world.trecord.service.feed.request.FeedInviteRequest;

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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void inviteUser(Long userFromId, Long feedId, FeedInviteRequest request) {
        FeedEntity feedEntity = findFeedOrException(feedId);

        ensureUserHasPermissionOverFeed(feedEntity, userFromId);

        UserEntity userToEntity = findUserOrException(request.getUserToId());

        if (Objects.equals(userToEntity.getId(), userFromId)) {
            return;
        }

        InvitationEntity invitationEntity = InvitationEntity.builder()
                .userToEntity(userToEntity)
                .feedEntity(feedEntity)
                .build();

        invitationRepository.save(invitationEntity);

        //TODO 피드 매니저에 추가

        eventPublisher.publishEvent(new NotificationEvent(userToEntity.getId(), userFromId, FEED_INVITATION, buildNotificationArgs(userToEntity, feedEntity)));
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private FeedEntity findFeedOrException(Long feedId) {
        return feedRepository.findById(feedId).orElseThrow(() -> new CustomException(FEED_NOT_FOUND));
    }

    private void ensureUserHasPermissionOverFeed(FeedEntity feedEntity, Long userId) {
        if (!feedEntity.isManagedBy(userId)) {
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
