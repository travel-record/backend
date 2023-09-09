package world.trecord.domain.users;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepositoryExtension {
    Optional<UserEntity> findByKeyword(String keyword);
}
