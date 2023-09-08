package world.trecord.service.notification.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.notification.NotificationEntity;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class NotificationListResponse {

    public List<NotificationResponse> notifications;

    @Builder
    private NotificationListResponse(List<NotificationEntity> notificationEntities) {
        this.notifications = notificationEntities.stream()
                .map(notificationEntity -> NotificationResponse.builder()
                        .notificationEntity(notificationEntity)
                        .build())
                .toList();
    }
}

