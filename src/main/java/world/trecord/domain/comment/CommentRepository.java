package world.trecord.domain.comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @EntityGraph(attributePaths = "userEntity")
    Optional<CommentEntity> findCommentEntityWithUserEntityById(@Param("id") Long commentId);
}
