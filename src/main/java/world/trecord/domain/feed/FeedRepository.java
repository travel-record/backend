package world.trecord.domain.feed;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import world.trecord.domain.users.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityOrderByStartAtDesc(UserEntity userEntity);

    @EntityGraph(attributePaths = {"userEntity"})
    Optional<FeedEntity> findFeedEntityWithUserEntityById(Long feedId);

    @Modifying
    @Query("UPDATE FeedEntity fe " +
            "SET fe.deletedDateTime = NOW() " +
            "WHERE fe = :feedEntity")
    void softDelete(@Param("feedEntity") FeedEntity feedEntity);
}
