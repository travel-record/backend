package world.trecord.dto.users.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import world.trecord.domain.users.UserEntity;

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

    public UserEntity toUpdateEntity() {
        return UserEntity.builder()
                .nickname(this.nickname)
                .imageUrl(this.imageUrl)
                .introduction(this.introduction)
                .build();
    }
}
