package world.trecord.domain.feed;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import world.trecord.domain.users.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityOrderByStartAtDesc(UserEntity userEntity);

    @EntityGraph(attributePaths = {"recordEntities"})
    Optional<FeedEntity> findFeedEntityWithRecordEntitiesById(@Param("id") Long feedId);
}
