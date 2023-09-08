package world.trecord.service.sse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.notification.NotificationEntity;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;
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
            this.senderId = args.getUserFrom().getId();
            this.senderNickname = args.getUserFrom().getNickname();
        }

        if (Objects.nonNull(args.getFeed())) {
            this.feedId = args.getFeed().getId();
        }

        if (Objects.nonNull(args.getRecord())) {
            this.recordId = args.getRecord().getId();
        }

        if (Objects.nonNull(args.getComment())) {
            this.commentId = args.getComment().getId();
        }
    }
}
