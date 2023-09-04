package world.trecord.service.users.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.users.UserEntity;

@NoArgsConstructor
@Getter
@Setter
public class UserInfoResponse {
    private String nickname;
    private String imageUrl;
    private String introduction;

    @Builder
    private UserInfoResponse(UserEntity userEntity) {
        this.nickname = userEntity.getNickname();
        this.imageUrl = userEntity.getImageUrl();
        this.introduction = userEntity.getIntroduction();
    }
}
