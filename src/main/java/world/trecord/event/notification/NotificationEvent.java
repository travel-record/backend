package world.trecord.event.notification;

import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.notification.enumeration.NotificationType;

public record NotificationEvent(Long userToId, Long userFromId, NotificationType type, NotificationArgs args) {
}
