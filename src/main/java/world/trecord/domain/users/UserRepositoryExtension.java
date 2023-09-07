package world.trecord.domain.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRepositoryExtension {
    Page<UserEntity> findByKeyword(String keyword, Pageable pageable);
}
