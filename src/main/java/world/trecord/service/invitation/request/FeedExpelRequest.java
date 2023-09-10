package world.trecord.service.invitation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class FeedExpelRequest {

    @NotNull
    private Long userToId;

    @Builder
    private FeedExpelRequest(Long userToId) {
        this.userToId = userToId;
    }
}
