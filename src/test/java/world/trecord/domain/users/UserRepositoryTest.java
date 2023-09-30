package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;
import java.util.Optional;

@Transactional
class UserRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmailTest() throws Exception {
        //given
        String email = "test@test.com";
        UserEntity userEntity = UserEntity.builder().email(email).build();

        userRepository.save(userEntity);

        //when
        Optional<UserEntity> optionalUser = userRepository.findByEmail(email);

        //then
        Assertions.assertThat(optionalUser)
                .isPresent()
                .hasValueSatisfying(user -> {
                    Assertions.assertThat(user.getId()).isNotNull();
                    Assertions.assertThat(user.getEmail()).isEqualTo(email);
                });
    }

    @Test
    @DisplayName("중복 닉네임을 조회한다")
    void existsByNicknameTest() throws Exception {
        //given
        String nickname = "nickname";
        String email = "test@test.com";

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .build();

        userRepository.save(userEntity);

        //when
        boolean result = userRepository.existsByNickname(nickname);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이미 저장된 이메일로 저장하려고 하면 예외가 발생한다")
    void uniqueKeyEmailTest() throws Exception {
        //given
        String email = "test@test.com";

        UserEntity userEntity1 = UserEntity.builder()
                .email(email)
                .nickname("nickname1")
                .build();

        userRepository.save(userEntity1);

        UserEntity userEntity2 = UserEntity.builder()
                .email(email)
                .nickname("nickname2")
                .build();

        //when // then
        Assertions.assertThatThrownBy(() -> userRepository.save(userEntity2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("이미 저장된 닉네임으로 저장하려고 하면 예외가 발생한다")
    void uniqueKeyNicknameTest() throws Exception {
        //given
        String nickname = "nickname";

        UserEntity userEntity1 = UserEntity.builder()
                .email("test1@test.com")
                .nickname(nickname)
                .build();

        userRepository.save(userEntity1);

        UserEntity userEntity2 = UserEntity.builder()
                .email("test2@test.com")
                .nickname(nickname)
                .build();

        //when // then
        Assertions.assertThatThrownBy(() -> userRepository.save(userEntity2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("사용자 아이디 리스트로 사용자 리스트를 조회한다")
    void findByIds_returnUserList() throws Exception {
        //given
        UserEntity user1 = UserEntityFixture.of();
        UserEntity user2 = UserEntityFixture.of();
        UserEntity user3 = UserEntityFixture.of();
        UserEntity user4 = UserEntityFixture.of();
        userRepository.saveAll(List.of(user1, user2, user3, user4));

        //when
        List<UserEntity> results = userRepository.findByIds(List.of(user1.getId(), user4.getId()));

        //then
        Assertions.assertThat(results)
                .hasSize(2)
                .extracting("id")
                .containsExactly(user1.getId(), user4.getId());
    }

}