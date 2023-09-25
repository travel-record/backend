package world.trecord.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryExtension {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM users", nativeQuery = true)
    void physicallyDeleteAll();
}
