package world.trecord.domain.record.projection;

import java.time.LocalDateTime;


public interface RecordWithFeedProjection {

    Long getId();

    String getTitle();

    String getPlace();

    String getImageUrl();

    LocalDateTime getDate();
}
