package world.trecord.web.service.users.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserInfoResponse {
    private String nickname;
    private String imageUrl;
    private String introduction;

    @Builder
    private UserInfoResponse(String nickname, String imageUrl, String introduction) {
        this.nickname = nickname;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
    }
}
