package world.trecord.domain.record;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.trecord.domain.record.projection.RecordWithFeedProjection;

import java.util.List;
import java.util.Optional;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    @Query("SELECT r " +
            "FROM RecordEntity r " +
            "LEFT JOIN FETCH r.commentEntities c " +
            "LEFT JOIN FETCH c.userEntity " +
            "JOIN FETCH r.feedEntity " +
            "WHERE r.id = :id " +
            "ORDER BY c.createdDateTime ASC")
    Optional<RecordEntity> findRecordEntityWithFeedEntityAndCommentEntitiesBy(@Param("id") Long recordId);

    @EntityGraph(attributePaths = {"commentEntities"})
    Optional<RecordEntity> findRecordEntityWithCommentEntitiesById(@Param("id") Long recordId);

    @Query("SELECT r.id as id, r.title as title, r.place as place, r.imageUrl as imageUrl , r.date as date " +
            "FROM RecordEntity r " +
            "WHERE r.feedEntity.id = :feedId " +
            "ORDER BY r.date ASC, r.createdDateTime ASC")
    List<RecordWithFeedProjection> findRecordEntityByFeedId(@Param("feedId") Long feedId);
}
