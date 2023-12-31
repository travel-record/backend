package world.trecord.domain.userrecordlike;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;

import java.util.Optional;

@Repository
public interface UserRecordLikeRepository extends JpaRepository<UserRecordLikeEntity, Long> {
    Optional<UserRecordLikeEntity> findByUserEntityIdAndRecordEntityId(Long userId, Long recordId);

    boolean existsByUserEntityIdAndRecordEntityId(Long userId, Long recordId);

    @Query("SELECT re.id as id, re.title as title, re.imageUrl as imageUrl , ue.id as authorId, ue.nickname as authorNickname " +
            "FROM UserRecordLikeEntity lrle " +
            "JOIN lrle.recordEntity re " +
            "JOIN re.feedEntity fe " +
            "JOIN fe.userEntity ue " +
            "WHERE lrle.userEntity.id = :userId " +
            "ORDER BY lrle.createdDateTime DESC ")
    Page<UserRecordProjection> findLikeRecordsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE UserRecordLikeEntity le " +
            "SET le.deletedDateTime = NOW() " +
            "where le.recordEntity.id = :recordId")
    void deleteAllByRecordEntityId(@Param("recordId") Long recordId);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM user_record_like", nativeQuery = true)
    void physicallyDeleteAll();
}
