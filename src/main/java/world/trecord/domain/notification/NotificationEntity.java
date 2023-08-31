package world.trecord.domain.notification;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "notification")
@SQLDelete(sql = "UPDATE notification SET deleted_date_time = NOW() WHERE id_notification = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class NotificationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20) default 'UNREAD'")
    private NotificationStatus status;

    @Column(name = "deleted_date_time", nullable = true)
    private LocalDateTime deletedDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users_to", nullable = true, foreignKey = @ForeignKey(name = "fk_notification_users_to"))
    private UserEntity usersToEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users_from", nullable = true, foreignKey = @ForeignKey(name = "fk_notification_users_from"))
    private UserEntity usersFromEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comment", nullable = true, foreignKey = @ForeignKey(name = "fk_notification_comment"))
    private CommentEntity commentEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_record", nullable = true, foreignKey = @ForeignKey(name = "fk_notification_record"))
    private RecordEntity recordEntity;

    @Builder
    private NotificationEntity(NotificationType type, NotificationStatus status, UserEntity usersToEntity, UserEntity usersFromEntity, CommentEntity commentEntity, RecordEntity recordEntity) {
        this.type = type;
        this.status = status;
        this.usersToEntity = usersToEntity;
        this.usersFromEntity = usersFromEntity;
        this.commentEntity = commentEntity;
        this.recordEntity = recordEntity;
    }

    public String getNotificationContent() {
        return type.getContent(this);
    }
}
