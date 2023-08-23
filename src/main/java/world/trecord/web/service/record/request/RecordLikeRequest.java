package world.trecord.web.service.record.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RecordLikeRequest {

    @NotNull
    private Long recordId;

    @Builder
    private RecordLikeRequest(Long recordId) {
        this.recordId = recordId;
    }
}
