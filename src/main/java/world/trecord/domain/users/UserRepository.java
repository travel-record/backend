package world.trecord.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Long>, UserRepositoryExtension {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("SELECT ue " +
            "FROM UserEntity ue " +
            "WHERE ue.id IN :userIds")
    List<UserEntity> findByIds(@Param("userIds") List<Long> userIds);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM users", nativeQuery = true)
    void physicallyDeleteAll();
}
