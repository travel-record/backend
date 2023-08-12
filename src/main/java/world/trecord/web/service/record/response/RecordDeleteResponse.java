package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RecordDeleteResponse {
    private Long recordId;

    @Builder
    private RecordDeleteResponse(Long recordId) {
        this.recordId = recordId;
    }
}
