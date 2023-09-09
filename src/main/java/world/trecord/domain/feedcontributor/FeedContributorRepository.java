package world.trecord.domain.feedcontributor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedContributorRepository extends JpaRepository<FeedContributorEntity, Long> {
    boolean existsByUserEntityIdAndFeedEntityId(Long userId, Long feedId);
}
