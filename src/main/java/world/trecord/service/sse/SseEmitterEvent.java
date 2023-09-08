package world.trecord.service.sse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.args.CommentArgs;
import world.trecord.domain.notification.args.FeedArgs;
import world.trecord.domain.notification.args.RecordArgs;
import world.trecord.domain.notification.args.UserFromArgs;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SseEmitterEvent {

    private NotificationType type;
    private NotificationStatus status;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;
    private UserFromArgs userFrom;
    private FeedArgs feed;
    private RecordArgs record;
    private CommentArgs comment;

    @Builder
    private SseEmitterEvent(NotificationEntity notificationEntity) {
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
        this.feed = notificationEntity.getArgs().getFeed();
        this.record = notificationEntity.getArgs().getRecord();
        this.comment = notificationEntity.getArgs().getComment();
    }
}
