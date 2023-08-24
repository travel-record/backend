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
                        .notificationEntity(notificationEntity)
                        .build())
                .toList();
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class Notification {

        private NotificationType type;
        private Long recordId;
        private String nickname;
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime date;

        @Builder
        private Notification(NotificationEntity notificationEntity) {
            // TODO type에 따라서 null 처리
            this.type = notificationEntity.getType();
            this.recordId = notificationEntity.getCommentEntity().getRecordEntity().getId();
            this.nickname = notificationEntity.getUsersFromEntity().getNickname();
            this.content = notificationEntity.getNotificationContent();
            this.date = notificationEntity.getCreatedDateTime();
        }
    }
}

