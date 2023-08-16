package world.trecord.domain.feed;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.trecord.domain.feed.projection.FeedWithRecordProjection;
import world.trecord.domain.users.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityOrderByStartAtDesc(UserEntity userEntity);

    @EntityGraph(attributePaths = {"recordEntities"})
    Optional<FeedEntity> findFeedEntityWithRecordEntitiesById(@Param("id") Long feedId);

    @Query("SELECT NEW world.trecord.domain.feed.projection.FeedWithRecordProjection(u.id,f.id, f.name, f.imageUrl, f.description, f.startAt, f.endAt, f.companion, f.place, f.satisfaction, r.id, r.title, r.place, r.date) " +
            "FROM FeedEntity f " +
            "LEFT JOIN f.userEntity u " +
            "LEFT JOIN f.recordEntities r " +
            "WHERE f.id = :id " +
            "ORDER BY r.date ASC, r.createdDateTime ASC")
    List<FeedWithRecordProjection> findWithUserEntityAndRecordEntitiesBy(@Param("id") Long feedId);
}
