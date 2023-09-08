package world.trecord.service.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.notification.args.CommentArgs;
import world.trecord.domain.notification.args.FeedArgs;
import world.trecord.domain.notification.args.RecordArgs;
import world.trecord.domain.notification.args.UserFromArgs;

import java.time.LocalDateTime;


@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Setter
@Getter
public class NotificationResponse {

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
    private NotificationResponse(NotificationEntity notificationEntity) {
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
        this.feed = notificationEntity.getArgs().getFeed();
        this.record = notificationEntity.getArgs().getRecord();
        this.comment = notificationEntity.getArgs().getComment();
    }
}
