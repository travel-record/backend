package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    @DisplayName("사용자 필드값을 수정하면 수정된 값을 가진다")
    void updateTest() throws Exception {
        //given
        String updatedNickname = "after nickname";
        String updatedIntroduction = "after introduction";
        String updatedImageUrl = "after image url";

        UserEntity userEntity = UserEntity.builder()
                .nickname("before nickname")
                .introduction("before introduction")
                .imageUrl("before image url")
                .build();

        UserEntity updateEntity = UserEntity.builder()
                .nickname(updatedNickname)
                .introduction(updatedIntroduction)
                .imageUrl(updatedImageUrl)
                .build();

        //when
        userEntity.update(updateEntity);

        //then
        Assertions.assertThat(userEntity)
                .extracting("nickname", "imageUrl", "introduction")
                .containsExactly(updatedNickname, updatedImageUrl, updatedIntroduction);
    }
}