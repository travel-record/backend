package world.trecord.domain.record;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
