package world.trecord.config.redis;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.security.account.UserContext;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.Optional;

@Transactional
class UserCacheRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("UserContext를 redis에 저장한다")
    void setUserContextTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        //when //then
        userCacheRepository.setUserContext(UserContext.fromEntity(userEntity));
    }

    @Test
    @DisplayName("redis에 키에 해당하는 UserContext가 있으면 반환한다")
    void getUserContextTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        userCacheRepository.setUserContext(UserContext.fromEntity(userEntity));

        //when
        Optional<UserContext> userContext = userCacheRepository.getUserContext(userEntity.getId());

        //then
        Assertions.assertThat(userContext).isPresent();
    }

    @Test
    @DisplayName("redis에 키에 해당하는 UserContext가 없으면 Optional.empty를 반환한다")
    void getUserContextWhenUserContextEmptyTest() throws Exception {
        //given
        Long notExistingUserId = 0L;

        //when
        Optional<UserContext> userContext = userCacheRepository.getUserContext(notExistingUserId);

        //then
        Assertions.assertThat(userContext).isEmpty();
    }
}