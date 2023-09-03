package world.trecord.service.sse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationStatus;
import world.trecord.domain.notification.NotificationType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SseNotificationEvent {

    private NotificationType type;
    private NotificationStatus status;
    private Long recordId;
    private Long commentId;
    private Long parentCommentId;
    private Long senderId;
    private String senderNickname;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;

    @Builder
    private SseNotificationEvent(NotificationEntity notificationEntity) {
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.recordId = notificationEntity.getArgs().getRecordId();
        this.commentId = notificationEntity.getArgs().getCommentId();
        this.senderId = notificationEntity.getArgs().getUserFromId();
        this.parentCommentId = notificationEntity.getArgs().getParentCommentId();
        this.senderNickname = notificationEntity.getArgs().getUserFromNickname();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
    }
}
