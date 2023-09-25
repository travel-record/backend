package world.trecord.domain.notification;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.notification.args.*;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.domain.users.UserEntity;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "notification")
@SQLDelete(sql = "UPDATE notification SET deleted_date_time = NOW() WHERE id_notification = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification", nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20) default 'UNREAD'")
    private NotificationStatus status;

    @Type(JsonType.class)
    @Column(name = "args", columnDefinition = "json")
    private NotificationArgs args;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users_to", foreignKey = @ForeignKey(name = "fk_notification_users_to"))
    private UserEntity usersToEntity;

    @Builder
    private NotificationEntity(NotificationType type, NotificationStatus status, UserEntity usersToEntity, NotificationArgs args) {
        this.type = type;
        this.status = status;
        this.usersToEntity = usersToEntity;
        this.args = args;
    }

    public String getNotificationContent() {
        return type.getContent(this);
    }

    public UserFromArgs getUserFromArgs() {
        return Objects.nonNull(this.args) ? args.getUserFrom() : null;
    }

    public FeedArgs getFeedArgs() {
        return Objects.nonNull(this.args) ? args.getFeed() : null;
    }

    public RecordArgs getRecordArgs() {
        return Objects.nonNull(this.args) ? args.getRecord() : null;
    }

    public CommentArgs getCommentArgs() {
        return Objects.nonNull(this.args) ? args.getComment() : null;
    }
}
