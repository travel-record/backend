package world.trecord.dto.feedcontributor.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.dto.users.response.UserInfoResponse;

import java.util.List;

@NoArgsConstructor
@Data
public class FeedInvitationHistoryResponse {

    private List<UserInfoResponse> content;

    public static FeedInvitationHistoryResponse of(List<Object[]> objects) {
        return FeedInvitationHistoryResponse.builder()
                .content(objects.stream().map(UserInfoResponse::of).toList())
                .build();
    }

    @Builder
    private FeedInvitationHistoryResponse(List<UserInfoResponse> content) {
        this.content = content;
    }
}
