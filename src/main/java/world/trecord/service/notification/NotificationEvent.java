package world.trecord.service.notification;

import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.notification.args.NotificationArgs;

public record NotificationEvent(Long userToId, Long userFromId, NotificationType type, NotificationArgs args) {
}
