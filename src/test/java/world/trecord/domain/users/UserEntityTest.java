package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    @DisplayName("사용자 정보 업데이트 테스트")
    void updateTest() throws Exception {
        //given
        String changeNickname = "after nickname";
        String changeIntroduction = "after introduction";
        String changeImageUrl = "after image url";

        UserEntity userEntity = UserEntity.builder()
                .nickname("before nickname")
                .introduction("before introduction")
                .imageUrl("before image url")
                .build();

        //when
        userEntity.update(changeNickname, changeImageUrl, changeIntroduction);

        //then
        Assertions.assertThat(userEntity.getNickname()).isEqualTo(changeNickname);
        Assertions.assertThat(userEntity.getImageUrl()).isEqualTo(changeImageUrl);
        Assertions.assertThat(userEntity.getIntroduction()).isEqualTo(changeIntroduction);
    }

}