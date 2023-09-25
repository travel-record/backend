package world.trecord.domain.userrecordlike;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "user_record_like",
        indexes = {
                @Index(name = "idx_user_record_like_user", columnList = "id_users"),
                @Index(name = "idx_user_record_like_record", columnList = "id_record"),
                @Index(name = "idx_user_record_like_user_record", columnList = "id_users, id_record")
        }
)
@SQLDelete(sql = "UPDATE user_record_like SET deleted_date_time = NOW() WHERE id_like = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class UserRecordLikeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_like", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", nullable = false, foreignKey = @ForeignKey(name = "fk_user_record_like_users"))
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_record", nullable = false, foreignKey = @ForeignKey(name = "fk_user_record_like_record"))
    private RecordEntity recordEntity;

    @Builder
    private UserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        this.userEntity = userEntity;
        this.recordEntity = recordEntity;
    }
}
