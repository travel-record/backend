package world.trecord.web.service.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationStatus;
import world.trecord.domain.notification.NotificationType;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Setter
@Getter
public class Notification {

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
    private Notification(NotificationEntity notificationEntity) {
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.recordId = notificationEntity.getArgs().getRecordId();
        this.commentId = notificationEntity.getArgs().getCommentId();
        this.parentCommentId = notificationEntity.getArgs().getParentCommentId();
        this.senderId = notificationEntity.getArgs().getUserFromId();
        this.senderNickname = notificationEntity.getArgs().getUserFromNickname();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
    }
}
