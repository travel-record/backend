package world.trecord.service.notification;

import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.notification.NotificationType;

public record NotificationEvent(Long userToId, Long userFromId, NotificationType type, NotificationArgs args) {
}
