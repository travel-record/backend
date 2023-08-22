package world.trecord.domain.userrecordlike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import java.util.Optional;

@Repository
public interface UserRecordLikeRepository extends JpaRepository<UserRecordLikeEntity, Long> {
    Optional<UserRecordLikeEntity> findUserRecordLikeEntityByUserEntityAndRecordEntity(UserEntity userEntity, RecordEntity recordEntity);
}
