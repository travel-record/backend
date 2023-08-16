package world.trecord.web.service.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;

@NoArgsConstructor
@Getter
@Setter
public class RecordDeleteResponse {
    private Long recordId;

    @Builder
    private RecordDeleteResponse(RecordEntity recordEntity) {
        this.recordId = recordEntity.getId();
    }
}
