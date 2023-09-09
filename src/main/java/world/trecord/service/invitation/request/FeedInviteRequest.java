package world.trecord.service.invitation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class FeedInviteRequest {

    @NotNull
    private Long userToId;

    @Builder
    private FeedInviteRequest(Long userToId) {
        this.userToId = userToId;
    }
}