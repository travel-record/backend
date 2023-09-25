package world.trecord.domain.feed;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    Page<FeedEntity> findByUserEntityId(Long userId, Pageable pageable);
    
    @Query("SELECT DISTINCT fe " +
            "FROM FeedEntity fe " +
            "JOIN FETCH fe.userEntity " +
            "LEFT JOIN FETCH fe.feedContributors fce " +
            "WHERE fe.id = :feedId AND (fce IS NULL OR fce.status = world.trecord.domain.feedcontributor.FeedContributorStatus.PARTICIPATING)")
    Optional<FeedEntity> findWithOwnerAndParticipatingContributorsById(@Param("feedId") Long feedId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fe " +
            "FROM FeedEntity fe " +
            "LEFT JOIN FETCH fe.feedContributors fc " +
            "WHERE fe.id = :feedId")
    Optional<FeedEntity> findWithFeedContributorsByIdForUpdate(@Param("feedId") Long feedId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM feed", nativeQuery = true)
    void physicallyDeleteAll();
}
