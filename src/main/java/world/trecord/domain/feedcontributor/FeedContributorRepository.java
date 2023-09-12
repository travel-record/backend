package world.trecord.domain.feedcontributor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface FeedContributorRepository extends JpaRepository<FeedContributorEntity, Long> {
    
    Optional<FeedContributorEntity> findByUserEntityIdAndFeedEntityId(Long userId, Long feedId);

    @Query("SELECT fce " +
            "FROM FeedContributorEntity fce " +
            "JOIN FETCH fce.feedEntity fe " +
            "JOIN FETCH fe.userEntity ue " +
            "WHERE fce.userEntity.id = :userId")
    Page<FeedContributorEntity> findWithFeedEntityByUserEntityId(@Param("userId") Long userId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE FeedContributorEntity fce " +
            "SET fce.status = :status, " +
            "fce.deletedDateTime = NOW() " +
            "WHERE fce.userEntity.id = :userId AND fce.feedEntity.id = :feedId")
    void updateStatusAndDeleteByUserEntityIdAndFeedEntityId(@Param("userId") Long userId,
                                                            @Param("feedId") Long feedId,
                                                            @Param("status") FeedContributorStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE FeedContributorEntity fce " +
            "SET fce.deletedDateTime = NOW() " +
            "where fce.feedEntity.id = :feedId")
    void deleteAllByFeedEntityId(@Param("feedId") Long feedId);

    void deleteByUserEntityIdAndFeedEntityId(Long userId, Long feedId);
}
