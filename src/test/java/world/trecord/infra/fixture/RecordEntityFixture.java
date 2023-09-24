package world.trecord.infra.fixture;

import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;

import java.time.LocalDateTime;

public abstract class RecordEntityFixture {

    public static RecordEntity of(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .longitude("longitude")
                .latitude("latitude")
                .date(LocalDateTime.of(2022, 3, 2, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }
}
