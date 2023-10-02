package world.trecord.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private NotificationStatus status;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;
    private UserFromArgs userFrom;
    private FeedArgs feed;
    private RecordArgs record;
    private CommentArgs comment;

    public static NotificationResponse of(NotificationEntity notificationEntity) {
        return NotificationResponse.builder()
                .notificationEntity(notificationEntity)
                .build();
    }

    @Builder
    private NotificationResponse(NotificationEntity notificationEntity) {
        this.id = notificationEntity.getId();
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
        this.userFrom = notificationEntity.getUserFromArgs();
        this.feed = notificationEntity.getFeedArgs();
        this.record = notificationEntity.getRecordArgs();
        this.comment = notificationEntity.getCommentArgs();
    }
}
