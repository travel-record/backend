package world.trecord.service.sse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class SseEmitterEvent {

    private NotificationType type;
    private NotificationStatus status;
    private Long recordId;
    private Long commentId;
    @JsonIgnore
    private Long recipientId;
    private Long parentCommentId;
    private Long senderId;
    private String senderNickname;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;

    @Builder
    private SseEmitterEvent(NotificationEntity notificationEntity) {
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.recordId = notificationEntity.getArgs().getRecordId();
        this.commentId = notificationEntity.getArgs().getCommentId();
        this.recipientId = notificationEntity.getUsersToEntity().getId();
        this.parentCommentId = notificationEntity.getArgs().getParentCommentId();
        this.senderId = notificationEntity.getArgs().getUserFromId();
        this.senderNickname = notificationEntity.getArgs().getUserFromNickname();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
    }
}
