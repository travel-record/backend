package world.trecord.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);

    boolean existsByNickname(String nickname);
}
