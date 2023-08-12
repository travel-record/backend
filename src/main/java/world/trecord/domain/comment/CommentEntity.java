package world.trecord.domain.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.trecord.domain.BaseEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "comment")
@Entity
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comment", nullable = false)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_record"))
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_record", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_user"))
    private RecordEntity recordEntity;

    @Builder
    private CommentEntity(String content, UserEntity userEntity, RecordEntity recordEntity) {
        this.content = content;
        this.userEntity = userEntity;
        if (recordEntity != null) {
            this.recordEntity = recordEntity;
            recordEntity.addCommentEntity(this);
        }
    }

    public void update(String content) {
        this.content = content;
    }
}
