package world.trecord.domain.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.trecord.domain.users.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<FeedEntity, Long> {
    List<FeedEntity> findByUserEntityOrderByStartAtDesc(UserEntity userEntity);

    @Query("SELECT f " +
            "FROM FeedEntity f " +
            "LEFT JOIN FETCH f.recordEntities " +
            "WHERE f.id = :id")
    Optional<FeedEntity> findWithRecordEntitiesBy(@Param("id") Long feedId);
}
