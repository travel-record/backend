package world.trecord.domain.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityIdOrderByStartAtDesc(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE FeedEntity fe " +
            "SET fe.deletedDateTime = NOW() " +
            "WHERE fe.id = :feedId")
    void softDeleteById(@Param("feedId") Long feedId);

    @Transactional
    @Modifying
    @Query("UPDATE FeedEntity fe " +
            "SET fe.deletedDateTime = NOW()")
    void softDeleteAll();
}
