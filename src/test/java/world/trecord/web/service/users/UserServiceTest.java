package world.trecord.web.service.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.IntegrationTestSupport;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.web.service.users.response.UserInfoResponse;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTestSupport
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("이메일로 새로운 사용자를 생성할 수 있다")
    void createUserWithEmailTest() throws Exception {
        //given
        String email = "test@test.com";

        //when
        UserEntity newUser = userService.createNewUserWith(email);

        //then
        assertThat(newUser.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("사용자 아이디로 사용자 정보를 조회할 수 있다")
    void findUserByUserIdTest() throws Exception {
        //given
        String email = "test@email.com";
        String nickname = "nickname";
        String imageUrl = "http://localhost/pictures";
        String introduction = "hello";
        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        UserEntity saveUser = userRepository.save(userEntity);

        //when
        UserInfoResponse response = userService.findUserBy(saveUser.getId());

        //then
        Assertions.assertThat(response.getNickname()).isEqualTo(nickname);
        Assertions.assertThat(response.getIntroduction()).isEqualTo(introduction);
        Assertions.assertThat(response.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 조회하면 예외가 발생한다")
    void findUserByNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when // then
        Assertions.assertThatThrownBy(() -> userService.findUserBy(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.NOT_EXISTING_USER);
    }

}