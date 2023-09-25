package world.trecord.infra.fixture;

import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.users.UserEntity;

public abstract class UserRecordLikeFixture {

    public static UserRecordLikeEntity of(UserEntity userEntity, RecordEntity recordEntity) {
        return UserRecordLikeEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();
    }
}
