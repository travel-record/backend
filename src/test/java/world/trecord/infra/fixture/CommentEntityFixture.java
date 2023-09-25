package world.trecord.infra.fixture;

import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

public abstract class CommentEntityFixture {

    public static CommentEntity of(UserEntity userEntity, RecordEntity recordEntity, CommentEntity parentCommentEntity) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .parentCommentEntity(parentCommentEntity)
                .content("content")
                .build();
    }

    public static CommentEntity of(UserEntity userEntity, RecordEntity recordEntity) {
        return of(userEntity, recordEntity, null);
    }
}
