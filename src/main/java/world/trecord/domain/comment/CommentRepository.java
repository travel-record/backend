package world.trecord.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c " +
            "FROM CommentEntity c " +
            "JOIN FETCH c.userEntity " +
            "WHERE c.id = :id")
    Optional<CommentEntity> findByIdWithUserEntity(@Param("id") Long commentId);
}
