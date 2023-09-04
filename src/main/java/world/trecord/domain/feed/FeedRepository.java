package world.trecord.domain.feed;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityIdOrderByStartAtDesc(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE FeedEntity fe " +
            "SET fe.deletedDateTime = NOW() " +
            "WHERE fe.id = :feedId")
    void softDeleteById(@Param("feedId") Long feedId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fe " +
            "FROM FeedEntity fe " +
            "WHERE fe.id = :feedId")
    Optional<FeedEntity> findByIdForUpdate(@Param("feedId") Long feedId);
}
