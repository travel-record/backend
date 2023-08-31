package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.web.security.Role;

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

    @Test
    @DisplayName("같은 id를 가지면 true를 반환한다")
    void isEqualToReturnsTrueTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity1, "id", 1L);

        UserEntity userEntity2 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity2, "id", 1L);

        //when
        boolean result = userEntity1.isEqualTo(userEntity2);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 id를 가지면 false를 반환한다")
    void isEqualToReturnsFalseTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity1, "id", 1L);

        UserEntity userEntity2 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity2, "id", 2L);

        //when
        boolean result = userEntity1.isEqualTo(userEntity2);

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("ROLE_USER을 반환한다")
    void getRoleTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder().build();

        //when
        String role = userEntity.getRole();

        //then
        Assertions.assertThat(role).isEqualTo(Role.ROLE_USER.name());
    }
}