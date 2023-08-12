package world.trecord.web.service.record.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RecordDeleteRequest {

    @NotNull
    private Long feedId;

    @NotNull
    private Long recordId;

    @Builder
    private RecordDeleteRequest(Long feedId, Long recordId) {
        this.feedId = feedId;
        this.recordId = recordId;
    }
}
