package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;

import java.util.List;

@Transactional
@IntegrationTestSupport
class UserRepositoryExtensionImplTest extends AbstractContainerBaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRepositoryExtension userRepositoryExtensionImpl;

    @Test
    @DisplayName("닉네임으로 사용자를 조회하여 반환한다")
    void findByKeywordTest() throws Exception {
        //given
        UserEntity userEntity1 = createUser("test1@email.com", "김김김");
        UserEntity userEntity2 = createUser("test2@email.com", "김이이");
        UserEntity userEntity3 = createUser("test3@email.com", "박박박");
        UserEntity userEntity4 = createUser("test4@email.com", "김이박");
        UserEntity userEntity5 = createUser("test5@email.com", "박이김");

        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3, userEntity4, userEntity5));

        PageRequest pageRequest = PageRequest.of(0, 10);
        String keyword = "김이";

        //when
        Page<UserEntity> result = userRepositoryExtensionImpl.findByKeyword(keyword, pageRequest);

        //then
        Assertions.assertThat(result.getContent()).containsExactly(userEntity2, userEntity4);
    }

    private UserEntity createUser(String email, String nickname) {
        return UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}