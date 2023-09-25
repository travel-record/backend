package world.trecord.dto.userrecordlike.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserRecordLikedResponse {

    private boolean liked;

    @Builder
    private UserRecordLikedResponse(boolean liked) {
        this.liked = liked;
    }
}
