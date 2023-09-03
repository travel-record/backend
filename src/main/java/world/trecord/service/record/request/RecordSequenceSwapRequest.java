package world.trecord.service.record.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class RecordSequenceSwapRequest {

    @NotNull
    private Long originalRecordId;

    @NotNull
    private Long targetRecordId;

    @Builder
    private RecordSequenceSwapRequest(Long originalRecordId, Long targetRecordId) {
        this.originalRecordId = originalRecordId;
        this.targetRecordId = targetRecordId;
    }
}
