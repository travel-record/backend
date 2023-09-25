package world.trecord.dto.userrecordlike.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;

@NoArgsConstructor
@Data
public class UserRecordLikeResponse {

    private Long recordId;
    private String title;
    private String imageUrl;
    private Long authorId;
    private String authorNickname;

    public static UserRecordLikeResponse of(UserRecordProjection projection) {
        return UserRecordLikeResponse.builder()
                .projection(projection)
                .build();
    }

    @Builder
    private UserRecordLikeResponse(UserRecordProjection projection) {
        this.recordId = projection.getId();
        this.title = projection.getTitle();
        this.imageUrl = projection.getImageUrl();
        this.authorId = projection.getAuthorId();
        this.authorNickname = projection.getAuthorNickname();
    }
}
