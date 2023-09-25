package world.trecord.infra.fixture;

import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;

public abstract class FeedEntityFixture {

    public static FeedEntity of(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(LocalDateTime.of(2022, 3, 2, 0, 0))
                .endAt(LocalDateTime.of(2022, 3, 10, 0, 0))
                .build();
    }
}
