package world.trecord.dto.feedcontributor.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.dto.users.response.UserResponse;

import java.util.List;

@NoArgsConstructor
@Data
public class FeedInvitationHistoryResponse {

    private List<UserResponse> content;

    public static FeedInvitationHistoryResponse of(List<Object[]> objects) {
        return FeedInvitationHistoryResponse.builder()
                .content(objects.stream().map(UserResponse::of).toList())
                .build();
    }

    @Builder
    private FeedInvitationHistoryResponse(List<UserResponse> content) {
        this.content = content;
    }
}
