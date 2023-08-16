package world.trecord.domain.record.projection;

import java.time.LocalDateTime;


public interface RecordWithFeedProjection {

    public Long getId();

    public String getTitle();

    public String getPlace();

    public LocalDateTime getDate();
}
