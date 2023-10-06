package world.trecord.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.projection.CommentRecordProjection;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT re.id as recordId, re.title as recordTitle, ce.id as commentId, ce.content as content, ce.createdDateTime as createdDateTime " +
            "FROM CommentEntity ce " +
            "JOIN ce.recordEntity re " +
            "WHERE ce.userEntity.id = :userId")
    Page<CommentRecordProjection> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT ce " +
            "FROM CommentEntity ce " +
            "JOIN FETCH ce.userEntity ue " +
            "LEFT JOIN ce.childCommentEntities cce " +
            "WHERE ce.recordEntity.id = :recordId AND ce.parentCommentEntity IS NULL " +
            "ORDER BY ce.createdDateTime ASC")
    Page<CommentEntity> findWithCommenterAndRepliesByRecordId(@Param("recordId") Long recordId, Pageable pageable);

    @EntityGraph(attributePaths = "userEntity")
    Page<CommentEntity> findWithUserEntityByParentCommentEntityId(Long parentCommentEntityId, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedDateTime = NOW() " +
            "where ce.recordEntity.id = :recordId")
    void deleteAllByRecordEntityId(@Param("recordId") Long recordId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedDateTime = NOW() " +
            "where ce.parentCommentEntity.id = :commentId")
    void deleteAllByCommentEntityId(@Param("commentId") Long commentId);
}
