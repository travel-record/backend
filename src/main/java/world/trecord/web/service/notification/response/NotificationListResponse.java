package world.trecord.web.service.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationStatus;
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
        private NotificationStatus status;
        private Long recordId;
        private Long commentId;
        private Long senderId;
        private String senderNickname;
        private String content;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
        private LocalDateTime date;

        @Builder
        private Notification(NotificationEntity notificationEntity) {
            this.type = notificationEntity.getType();
            this.status = notificationEntity.getStatus();
            this.recordId = notificationEntity.getRecordEntity() != null ? notificationEntity.getRecordEntity().getId() : null;
            this.commentId = notificationEntity.getCommentEntity() != null ? notificationEntity.getCommentEntity().getId() : null;
            this.senderId = notificationEntity.getUsersFromEntity() != null ? notificationEntity.getUsersFromEntity().getId() : null;
            this.senderNickname = notificationEntity.getUsersFromEntity() != null ? notificationEntity.getUsersFromEntity().getNickname() : null;
            this.content = notificationEntity.getNotificationContent();
            this.date = notificationEntity.getCreatedDateTime();
        }
    }
}

