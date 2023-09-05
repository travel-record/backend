package world.trecord.domain.record;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RecordSequenceRepository extends JpaRepository<RecordSequenceEntity, Long> {

    Optional<RecordSequenceEntity> findByFeedEntityIdAndDate(Long feedId, LocalDateTime date);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO record_sequence (id_feed, date, sequence) " +
            "VALUES (:feedId, :date, 1) " +
            "ON DUPLICATE KEY UPDATE sequence = sequence + 1", nativeQuery = true)
    void insertOrIncrement(@Param("feedId") Long feedId, @Param("date") LocalDateTime date);


    @Modifying
    @Transactional
    @Query("UPDATE RecordSequenceEntity rse " +
            "SET rse.deletedDateTime = NOW() " +
            "where rse.feedEntity.id = :feedId")
    void deleteAllByFeedEntityId(@Param("feedId") Long feedId);
}