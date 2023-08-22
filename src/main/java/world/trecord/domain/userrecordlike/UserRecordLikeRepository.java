package world.trecord.domain.userrecordlike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecordLikeRepository extends JpaRepository<UserRecordLikeEntity, Long> {
}
