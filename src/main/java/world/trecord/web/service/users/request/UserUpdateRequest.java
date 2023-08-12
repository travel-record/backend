package world.trecord.web.service.users.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class UserUpdateRequest {

    @NotEmpty
    private String nickname;

    private String imageUrl;

    private String introduction;

    @Builder
    private UserUpdateRequest(String nickname, String imageUrl, String introduction) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
    }
}
