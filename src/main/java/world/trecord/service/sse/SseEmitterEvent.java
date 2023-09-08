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
import world.trecord.domain.notification.args.NotificationArgs;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SseEmitterEvent {

    // notificationEntity
    private NotificationType type;
    private NotificationStatus status;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;
    private Long recipientId;

    // userFrom
    private Long senderId;
    private String senderNickname;

    // feed
    private Long feedId;

    // record
    private Long recordId;

    // comment
    private Long commentId;

    @Builder
    private SseEmitterEvent(NotificationEntity notificationEntity) {
        this.type = notificationEntity.getType();
        this.status = notificationEntity.getStatus();
        this.content = notificationEntity.getNotificationContent();
        this.date = notificationEntity.getCreatedDateTime();
        this.recipientId = notificationEntity.getUsersToEntity().getId();

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
