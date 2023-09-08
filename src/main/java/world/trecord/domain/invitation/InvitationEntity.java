package world.trecord.domain.invitation;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;

import static world.trecord.domain.invitation.InvitationStatus.COMPLETED;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "invitation")
@SQLDelete(sql = "UPDATE invitation SET deleted_date_time = NOW() WHERE id_invitation = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class InvitationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_invitation", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users_to", foreignKey = @ForeignKey(name = "fk_invitation_users"))
    private UserEntity userToEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_feed", foreignKey = @ForeignKey(name = "fk_invitation_feed"))
    private FeedEntity feedEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvitationStatus status;

    @Builder
    private InvitationEntity(UserEntity userToEntity, FeedEntity feedEntity) {
        this.userToEntity = userToEntity;
        this.feedEntity = feedEntity;
        this.status = COMPLETED;
    }
}
