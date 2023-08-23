package world.trecord.domain.userrecordlike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;
import world.trecord.domain.users.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRecordLikeRepository extends JpaRepository<UserRecordLikeEntity, Long> {
    Optional<UserRecordLikeEntity> findUserRecordLikeEntityByUserEntityAndRecordEntity(UserEntity userEntity, RecordEntity recordEntity);

    @Query("SELECT re.id as id, re.title as title, re.imageUrl as imageUrl , ue.id as authorId, ue.nickname as authorNickname " +
            "FROM UserRecordLikeEntity lrle " +
            "JOIN lrle.recordEntity re " +
            "JOIN re.feedEntity fe " +
            "JOIN fe.userEntity ue " +
            "WHERE lrle.userEntity = :userEntity " +
            "ORDER BY lrle.createdDateTime DESC ")
    List<UserRecordProjection> findLikedRecordsByUserEntity(@Param("userEntity") UserEntity userEntity);
}
