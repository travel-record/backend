package world.trecord.infra.fixture;

import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import java.time.LocalDateTime;

public abstract class RecordEntityFixture {

    public static RecordEntity of(FeedEntity feedEntity) {
        return of(feedEntity.getUserEntity(), feedEntity, 1);
    }

    public static RecordEntity of(UserEntity userEntity, FeedEntity feedEntity, int sequence) {
        return RecordEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .title("record")
                .longitude("longitude")
                .latitude("latitude")
                .place("place")
                .date(LocalDateTime.of(2022, 10, 1, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }
}
