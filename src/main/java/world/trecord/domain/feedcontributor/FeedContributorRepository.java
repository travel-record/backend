package world.trecord.domain.feedcontributor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FeedContributorRepository extends JpaRepository<FeedContributorEntity, Long> {
    boolean existsByUserEntityIdAndFeedEntityId(Long userId, Long feedId);

    @Transactional
    @Modifying
    @Query("UPDATE FeedContributorEntity fce " +
            "SET fce.deletedDateTime = NOW() " +
            "where fce.feedEntity.id = :feedId")
    void deleteAllByFeedEntityId(@Param("feedId") Long feedId);
}
