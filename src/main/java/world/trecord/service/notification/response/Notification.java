package world.trecord.service.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.NotificationStatus;
import world.trecord.domain.notification.NotificationType;
import world.trecord.domain.notification.args.NotificationArgs;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Setter
@Getter
public class Notification {

    private NotificationType type;
    private NotificationStatus status;
    private Long feedId;
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
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();

        NotificationArgs args = notificationEntity.getArgs();

        if (Objects.nonNull(args.getUserFrom())) {
            this.senderId = args.getUserFrom().getUserFromId();
            this.senderNickname = args.getUserFrom().getUserFromNickname();
        }

        if (Objects.nonNull(args.getFeed())) {
            this.feedId = args.getFeed().getFeedId();
        }

        if (Objects.nonNull(args.getRecord())) {
            this.recordId = args.getRecord().getRecordId();
        }

        if (Objects.nonNull(args.getComment())) {
            this.commentId = args.getComment().getCommentId();
        }
    }
}
