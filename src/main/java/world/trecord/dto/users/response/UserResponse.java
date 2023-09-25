package world.trecord.dto.users.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import world.trecord.domain.users.UserEntity;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserResponse {
    private Long userId;
    private String nickname;
    private String imageUrl;
    private String introduction;

    public static UserResponse of(UserEntity userEntity) {
        return UserResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    public static UserResponse of(Object[] obj) {
        return new UserResponse((Long) obj[0], (String) obj[1], (String) obj[2], (String) obj[3]);
    }

    @Builder
    private UserResponse(UserEntity userEntity) {
        this.userId = userEntity.getId();
        this.nickname = userEntity.getNickname();
        this.imageUrl = userEntity.getImageUrl();
        this.introduction = userEntity.getIntroduction();
    }
}
