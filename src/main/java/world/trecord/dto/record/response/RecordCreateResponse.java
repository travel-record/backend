package world.trecord.dto.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;

@NoArgsConstructor
@Setter
@Getter
public class RecordCreateResponse {

    private Long writerId;
    private Long feedId;
    private Long recordId;

    public static RecordCreateResponse of(RecordEntity recordEntity) {
        return RecordCreateResponse.builder()
                .recordEntity(recordEntity)
                .build();
    }

    @Builder
    private RecordCreateResponse(RecordEntity recordEntity) {
        this.writerId = recordEntity.getUserId();
        this.feedId = recordEntity.getFeedId();
        this.recordId = recordEntity.getId();
    }
}
