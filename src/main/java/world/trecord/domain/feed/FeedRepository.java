package world.trecord.domain.feed;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityIdOrderByStartAtDesc(Long userId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fe " +
            "FROM FeedEntity fe " +
            "LEFT JOIN FETCH fe.feedContributors fc " +
            "WHERE fe.id = :feedId")
    Optional<FeedEntity> findWithFeedContributorsByIdForUpdate(@Param("feedId") Long feedId);
}
