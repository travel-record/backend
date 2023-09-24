package world.trecord.infra.fixture;

import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

public abstract class CommentEntityFixture {

    public static CommentEntity of(UserEntity userEntity, RecordEntity recordEntity, String content) {
        return CommentEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .content(content)
                .build();
    }

    public static CommentEntity of(UserEntity userEntity, RecordEntity recordEntity) {
        return of(userEntity, recordEntity, "content");
    }
}
