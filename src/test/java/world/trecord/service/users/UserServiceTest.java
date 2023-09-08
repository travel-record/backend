package world.trecord.service.users;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.users.request.UserUpdateRequest;
import world.trecord.service.users.response.UserInfoResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static world.trecord.exception.CustomExceptionError.NICKNAME_DUPLICATED;

@Slf4j
@Transactional
@IntegrationTestSupport
class UserServiceTest extends AbstractContainerBaseTest {

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
        UserEntity newUser = userService.createNewUser(email);

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
        UserInfoResponse response = userService.getUser(saveUser.getId());

        //then
        Assertions.assertThat(response)
                .extracting("nickname", "introduction", "imageUrl")
                .containsExactly(nickname, introduction, imageUrl);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 아이디로 조회하면 예외가 발생한다")
    void findUserByNotExistingUserIdTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when // then
        Assertions.assertThatThrownBy(() -> userService.getUser(notExistingUserId))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }


    @Test
    @DisplayName("새로운 닉네임으로 업데이트 한다")
    void updateUserTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test1@email.com", "nickname"));

        String changedNickname = "changed nickname";

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname(changedNickname)
                .build();

        //when
        userService.updateUser(userEntity.getId(), updateRequest);

        //then
        Assertions.assertThat(userRepository.findById(userEntity.getId()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    Assertions.assertThat(user.getNickname()).isEqualTo(changedNickname);
                });
    }

    @Test
    @DisplayName("이미 저장된 닉네임으로 업데이트 요청하면 예외가 발생한다")
    void updateUserWhenDuplicatedNicknameTest() throws Exception {
        //given
        String savedNickname = "nickname";
        userRepository.save(createUser("test@email.com", savedNickname));
        UserEntity userEntity = userRepository.save(createUser("test1@email.com", "nickname1"));

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname(savedNickname)
                .build();

        //when
        Assertions.assertThatThrownBy(() -> userService.updateUser(userEntity.getId(), updateRequest))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(NICKNAME_DUPLICATED);
    }

    @Test
    @DisplayName("사용자 정보를 업데이트한다")
    void updateUserWhenNewDescTest() throws Exception {
        //given
        String originalNickname = "nickname";
        String beforeIntroduction = "before introduction";
        String email = "test1@email.com";

        UserEntity userEntity = userRepository.save(UserEntity.builder()
                .email(email)
                .nickname(originalNickname)
                .introduction(beforeIntroduction)
                .build());

        String changedIntroduction = "change introduction";
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .nickname(originalNickname)
                .introduction(changedIntroduction)
                .build();

        //when
        userService.updateUser(userEntity.getId(), updateRequest);

        //then
        Assertions.assertThat(userRepository.findById(userEntity.getId()))
                .isPresent()
                .hasValueSatisfying(user -> {
                    Assertions.assertThat(user.getNickname()).isEqualTo(originalNickname);
                    Assertions.assertThat(user.getIntroduction()).isEqualTo(changedIntroduction);
                });
    }

    @Test
    @DisplayName("사용자를 조회하여 UserContext로 반환한다")
    void loadUserContextByUserIdTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com", "nickname"));

        //when
        UserContext userContext = userService.getUserContextOrException(userEntity.getId());

        //then
        Assertions.assertThat(userContext.getId()).isEqualTo(userEntity.getId());
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 CustomException 예외가 발생한다")
    void loadUserContextByUserIdWhenUserNotFoundTest() throws Exception {
        //given
        long notExistingUserId = -1L;

        //when //then
        Assertions.assertThatThrownBy(() -> userService.getUserContextOrException(notExistingUserId))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("닉네임으로 사용자를 조회하여 반환한다")
    void searchUserTest() throws Exception {
        //given
        UserEntity userEntity1 = createUser("test1@email.com", "김");
        UserEntity userEntity2 = createUser("test2@email.com", "이이이");
        UserEntity userEntity3 = createUser("test3@email.com", "김박박");
        UserEntity userEntity4 = createUser("test4@email.com", "김이박");
        UserEntity userEntity5 = createUser("test5@email.com", "박이김");

        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3, userEntity4, userEntity5));

        String keyword = "김";

        //when
        UserInfoResponse response = userService.searchUser(keyword);

        //then
        Assertions.assertThat(response)
                .extracting("userId")
                .isEqualTo(userEntity1.getId());
    }

    @Test
    @DisplayName("닉네임에 포함되는 사용자가 존재하지 않으면 null을 반환한다")
    void searchUserWhenUserEmptyTest() throws Exception {
        //given
        PageRequest pageRequest = PageRequest.of(0, 10);
        String keyword = "김";

        //when
        UserInfoResponse response = userService.searchUser(keyword);

        //then
        Assertions.assertThat(response).isNull();
    }

    private UserEntity createUser(String email, String nickname) {
        return UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}