package world.trecord.web.service.userrecordlike.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserRecordLikeResponse {

    private boolean liked;

    @Builder
    private UserRecordLikeResponse(boolean liked) {
        this.liked = liked;
    }
}
