package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class RecordSequenceSwapResponse {

    private Long originalRecordId;
    private Long targetRecordId;

    @Builder
    private RecordSequenceSwapResponse(Long originalRecordId, Long targetRecordId) {
        this.originalRecordId = originalRecordId;
        this.targetRecordId = targetRecordId;
    }
}
