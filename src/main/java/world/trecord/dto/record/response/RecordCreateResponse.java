package world.trecord.dto.record.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

@NoArgsConstructor
@Setter
@Getter
public class RecordCreateResponse {

    private Long writerId;
    private Long feedId;
    private Long recordId;

    @Builder
    private RecordCreateResponse(UserEntity writerEntity, RecordEntity recordEntity) {
        this.writerId = writerEntity.getId();
        this.feedId = recordEntity.getFeedEntity().getId();
        this.recordId = recordEntity.getId();
    }
}
