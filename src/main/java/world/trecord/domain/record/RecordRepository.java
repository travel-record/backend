package world.trecord.domain.record;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.record.projection.RecordWithFeedProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    @EntityGraph(attributePaths = {"feedEntity"})
    Optional<RecordEntity> findWithFeedEntityById(Long recordId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT re " +
            "FROM RecordEntity re " +
            "WHERE re.id = :recordId")
    Optional<RecordEntity> findByIdForUpdate(@Param("recordId") Long recordId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT re " +
            "FROM RecordEntity re " +
            "WHERE re.id IN :recordIds")
    List<RecordEntity> findByIdsForUpdate(@Param("recordIds") List<Long> recordIds);

    // TODO delete
    @Query("SELECT re.id as id, re.title as title, re.place.place as place, re.imageUrl as imageUrl , re.date as date " +
            "FROM RecordEntity re " +
            "WHERE re.feedEntity.id = :feedId " +
            "ORDER BY re.date ASC, re.sequence ASC ,re.createdDateTime ASC")
    List<RecordWithFeedProjection> findRecordsByFeedEntityId(@Param("feedId") Long feedId);

    @Query("SELECT re.id as id, re.title as title, re.place.place as place, re.imageUrl as imageUrl , re.date as date " +
            "FROM RecordEntity re " +
            "WHERE re.feedEntity.id = :feedId")
    Page<RecordWithFeedProjection> findRecordListByFeedEntityId(Long feedId, Pageable pageable);

    @Query("SELECT MAX(re.sequence) " +
            "FROM RecordEntity re " +
            "WHERE re.feedEntity.id = :feedId AND re.date = :date")
    Optional<Integer> findMaxSequenceByFeedEntityIdAndDate(@Param("feedId") Long feedId, @Param("date") LocalDateTime date);

    @Transactional
    @Modifying
    @Query("UPDATE RecordEntity re " +
            "SET re.deletedDateTime = NOW() " +
            "WHERE re.feedEntity.id = :feedId AND re.userEntity.id = :userId")
    void deleteByFeedEntityIdAndUserEntityId(@Param("feedId") Long feedId, @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE RecordEntity re " +
            "SET re.deletedDateTime = NOW() " +
            "where re.feedEntity.id = :feedId")
    void deleteAllByFeedEntityId(@Param("feedId") Long feedId);
}
