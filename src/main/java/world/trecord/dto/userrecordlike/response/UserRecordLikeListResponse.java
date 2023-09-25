package world.trecord.dto.userrecordlike.response;

import lombok.*;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;

import java.util.List;

@NoArgsConstructor
@Data
public class UserRecordLikeListResponse {

    private List<Record> records;

    @Builder
    private UserRecordLikeListResponse(List<UserRecordProjection> projectionList) {
        this.records = projectionList.stream().map(Record::new).toList();
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Record {
        private Long recordId;
        private String title;
        private String imageUrl;
        private Long authorId;
        private String authorNickname;

        public Record(UserRecordProjection projection) {
            this.recordId = projection.getId();
            this.title = projection.getTitle();
            this.imageUrl = projection.getImageUrl();
            this.authorId = projection.getAuthorId();
            this.authorNickname = projection.getAuthorNickname();
        }
    }
}
