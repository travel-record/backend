package world.trecord.domain.record;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.projection.RecordWithFeedProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    @EntityGraph(attributePaths = {"feedEntity"})
    Optional<RecordEntity> findRecordEntityWithFeedEntityById(Long recordId);

    @Query("SELECT r.id as id, r.title as title, r.place as place, r.imageUrl as imageUrl , r.date as date " +
            "FROM RecordEntity r " +
            "WHERE r.feedEntity.id = :feedId " +
            "ORDER BY r.date ASC, r.sequence ASC ,r.createdDateTime ASC")
    List<RecordWithFeedProjection> findRecordEntityByFeedId(@Param("feedId") Long feedId);

    @Query("SELECT MAX(r.sequence) " +
            "FROM RecordEntity r " +
            "WHERE r.feedEntity.id = :feedId AND r.date = :date")
    Optional<Integer> findMaxSequenceByFeedAndDate(@Param("feedId") Long feedId, @Param("date") LocalDateTime date);

    @Transactional
    @Modifying
    @Query("UPDATE RecordEntity re " +
            "SET re.deletedDateTime = NOW() " +
            "where re.feedEntity = :feedEntity")
    void deleteAllByFeedEntity(@Param("feedEntity") FeedEntity feedEntity);

    @Transactional
    @Modifying
    @Query("UPDATE RecordEntity re " +
            "SET re.deletedDateTime = NOW() " +
            "WHERE re = :recordEntity")
    void softDelete(@Param("recordEntity") RecordEntity recordEntity);
}
