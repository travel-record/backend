package world.trecord.domain.feedcontributor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedContributorRepository extends JpaRepository<FeedContributorEntity, Long> {

    @Query(value = """
            WITH InvitedContributors AS (
                SELECT 
                    u.id_users AS id_users, u.nickname AS nickname, u.image_url AS image_url, u.introduction AS introduction,
                    ROW_NUMBER() OVER (PARTITION BY u.id_users ORDER BY fc.created_date_time DESC) AS row_num
                FROM 
                    feed_contributor fc
                JOIN 
                    users u ON fc.id_users = u.id_users
                WHERE 
                    fc.id_feed IN (
                        SELECT f.id_feed 
                        FROM feed f 
                        WHERE f.id_owner = :userId
                    ) 
            )
            SELECT 
                id_users, nickname, image_url, introduction
            FROM 
                InvitedContributors
            WHERE 
                row_num = 1
            ORDER BY 
                id_users DESC
            LIMIT 3 """, nativeQuery = true)
    List<Object[]> findRecentMaxThreeContributorsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * " +
            "FROM feed_contributor fc " +
            "WHERE fc.id_users = :userId AND fc.id_feed = :feedId " +
            "ORDER BY fc.modified_date_time DESC " +
            "LIMIT 1", nativeQuery = true)
    Optional<FeedContributorEntity> findTopByUserIdAndFeedIdOrderByModifiedAtDesc(Long userId, Long feedId);

    Optional<FeedContributorEntity> findByUserEntityIdAndFeedEntityId(Long userId, Long feedId);

    @EntityGraph(attributePaths = {"feedEntity", "userEntity"})
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

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM feed_contributor", nativeQuery = true)
    void physicallyDeleteAll();
}
