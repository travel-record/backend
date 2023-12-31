package world.trecord.domain.comment;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "comment",
        indexes = {
                @Index(name = "idx_comment_users", columnList = "id_users"),
                @Index(name = "idx_comment_record", columnList = "id_record"),
                @Index(name = "idx_comment_parent", columnList = "id_parent")
        }
)
@SQLDelete(sql = "UPDATE comment SET deleted_date_time = NOW() WHERE id_comment = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comment", nullable = false, updatable = false)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_user"))
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_record", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_record"))
    private RecordEntity recordEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent", foreignKey = @ForeignKey(name = "fk_comment_comment"))
    private CommentEntity parentCommentEntity;

    @OneToMany(mappedBy = "parentCommentEntity", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CommentEntity> childCommentEntities = new ArrayList<>();

    @Builder
    private CommentEntity(String content, UserEntity userEntity, RecordEntity recordEntity, CommentEntity parentCommentEntity) {
        this.content = content;
        this.userEntity = userEntity;
        this.recordEntity = recordEntity;
        if (parentCommentEntity != null) {
            this.parentCommentEntity = parentCommentEntity;
            parentCommentEntity.addChildCommentEntity(this);
        }
    }

    public void addChildCommentEntity(CommentEntity childCommentEntity) {
        this.childCommentEntities.add(childCommentEntity);
    }

    public void update(CommentEntity updateEntity) {
        this.content = updateEntity.getContent();
    }

    public boolean isCommenter(Long userId) {
        return Objects.equals(this.userEntity.getId(), userId);
    }

    public Long getParentCommentId() {
        return Objects.nonNull(this.parentCommentEntity) ? this.parentCommentEntity.getId() : null;
    }

    public Long getRecordId() {
        return Objects.nonNull(this.recordEntity) ? this.recordEntity.getId() : null;
    }

    public Long getUserId() {
        return Objects.nonNull(this.userEntity) ? this.userEntity.getId() : null;
    }

    public String getUserNickname() {
        return Objects.nonNull(this.userEntity) ? this.userEntity.getNickname() : null;
    }

    public String getUserImageUrl() {
        return Objects.nonNull(this.userEntity) ? this.userEntity.getImageUrl() : null;
    }

    public int getReplyCount() {
        return Objects.nonNull(this.childCommentEntities) ? this.childCommentEntities.size() : 0;
    }
}