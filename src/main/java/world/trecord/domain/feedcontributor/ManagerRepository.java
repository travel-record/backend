package world.trecord.domain.feedcontributor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<ManagerEntity, Long> {
    boolean existsByUserEntityIdAndFeedEntityId(Long userId, Long feedId);
}
