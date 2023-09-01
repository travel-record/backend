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
@Table(name = "comment")
@SQLDelete(sql = "UPDATE comment SET deleted_date_time = NOW() WHERE id_comment = ?")
@Where(clause = "deleted_date_time is NULL")
@Entity
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comment", nullable = false)
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

    @OneToMany(mappedBy = "parentCommentEntity")
    private List<CommentEntity> childCommentEntities = new ArrayList<>();

    @Builder
    private CommentEntity(String content, UserEntity userEntity, RecordEntity recordEntity, CommentEntity parentCommentEntity) {
        this.content = content;
        this.userEntity = userEntity;
        if (recordEntity != null) {
            this.recordEntity = recordEntity;
            recordEntity.addCommentEntity(this);
        }
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
}