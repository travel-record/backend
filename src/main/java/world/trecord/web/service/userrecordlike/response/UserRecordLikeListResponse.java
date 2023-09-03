package world.trecord.web.service.userrecordlike.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
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
