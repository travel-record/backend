package world.trecord.service.users;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.security.account.UserContext;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.users.request.UserUpdateRequest;
import world.trecord.dto.users.response.UserResponse;
import world.trecord.exception.CustomException;
import world.trecord.exception.CustomExceptionError;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;

import static world.trecord.exception.CustomExceptionError.NICKNAME_DUPLICATED;

@Slf4j
@Transactional
class UserServiceTest extends AbstractIntegrationTest {

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
        UserResponse response = userService.getUser(saveUser.getId());

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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test1@email.com", "nickname"));

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
        userRepository.save(UserEntityFixture.of("test@email.com", savedNickname));
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test1@email.com", "nickname1"));

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
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test@email.com", "nickname"));

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
        UserEntity userEntity1 = UserEntityFixture.of("test1@email.com", "김");
        UserEntity userEntity2 = UserEntityFixture.of("test2@email.com", "이이이");
        UserEntity userEntity3 = UserEntityFixture.of("test3@email.com", "김박박");
        UserEntity userEntity4 = UserEntityFixture.of("test4@email.com", "김이박");
        UserEntity userEntity5 = UserEntityFixture.of("test5@email.com", "박이김");

        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3, userEntity4, userEntity5));

        String keyword = "김";

        //when
        UserResponse response = userService.searchUser(userEntity5.getId(), keyword);

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
        UserResponse response = userService.searchUser(0L, keyword);

        //then
        Assertions.assertThat(response).isNull();
    }

    @Test
    @DisplayName("자신의 닉네임으로 사용자를 검색하는 경우 검색 결과에서 제외된다")
    void searchUserWhenSelfNicknameTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of("test1@email.com", "김"));
        String keyword = "김";

        //when
        UserResponse response = userService.searchUser(userEntity.getId(), keyword);

        //then
        Assertions.assertThat(response).isNull();
    }

    @Test
    @DisplayName("사용자 아이디 리스트로 사용자를 조회하여 리스트로 반환한다")
    void findUsersOrException_returnList() throws Exception {
        //given
        UserEntity user1 = UserEntityFixture.of();
        UserEntity user2 = UserEntityFixture.of();
        UserEntity user3 = UserEntityFixture.of();
        UserEntity user4 = UserEntityFixture.of();
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        //when
        List<UserEntity> userEntityList = userService.findUsersOrException(List.of(user1.getId(), user4.getId()));

        //then
        Assertions.assertThat(userEntityList)
                .hasSize(2)
                .extracting("id")
                .containsExactly(user1.getId(), user4.getId());
    }

    @Test
    @DisplayName("사용자 아이디 리스트로 사용자를 조회 시 존재하지 않는 사용자가 있으면 예외를 던진다")
    void findUsersOrException_whenUserNotFound_throwException() throws Exception {
        //given
        UserEntity user1 = UserEntityFixture.of();
        UserEntity user2 = UserEntityFixture.of();
        UserEntity user3 = UserEntityFixture.of();
        UserEntity user4 = UserEntityFixture.of();
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        long notExistingUserId = -1L;

        //when //then
        Assertions.assertThatThrownBy(() -> userService.findUsersOrException(List.of(notExistingUserId, user1.getId(), user2.getId(), user3.getId(), user4.getId())))
                .isInstanceOf(CustomException.class)
                .extracting("error")
                .isEqualTo(CustomExceptionError.USER_NOT_FOUND);
    }
}