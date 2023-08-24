package world.trecord.web.service.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class NotificationListResponse {

    public List<Notification> notifications;

    @Builder
    private NotificationListResponse(List<NotificationEntity> notificationEntities) {
        this.notifications = notificationEntities.stream()
                .map(notificationEntity -> Notification.builder()
                        .type(notificationEntity.getType())
                        .nickname(notificationEntity.getUsersFromEntity().getNickname())
                        .date(notificationEntity.getCreatedDateTime())
                        .content(notificationEntity.getNotificationContent())
                        .build())
                .toList();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Notification {

        private NotificationType type;
        // TODO 필드 추가
        private String nickname;
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime date;

        @Builder
        private Notification(NotificationType type, String nickname, String content, LocalDateTime date) {
            this.type = type;
            this.nickname = nickname;
            this.content = content;
            this.date = date;
        }
    }
}

