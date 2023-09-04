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

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @EntityGraph(attributePaths = "userEntity")
    Optional<CommentEntity> findWithUserEntityById(Long commentId);

    @EntityGraph(attributePaths = "childCommentEntities")
    Optional<CommentEntity> findWithChildCommentEntitiesById(Long commentId);

    @Query("SELECT re.id as recordId, re.title as recordTitle, ce.id as commentId, ce.content as content, ce.createdDateTime as createdDateTime " +
            "FROM CommentEntity ce " +
            "JOIN ce.recordEntity re " +
            "WHERE ce.userEntity.id = :userId " +
            "ORDER BY ce.createdDateTime DESC")
    List<CommentRecordProjection> findByUserEntityIdOrderByCreatedDateTimeDesc(@Param("userId") Long userId);

    @EntityGraph(attributePaths = "userEntity")
    List<CommentEntity> findWithUserEntityByRecordEntityIdOrderByCreatedDateTimeAsc(Long recordId);

    Page<CommentEntity> findByParentCommentEntityId(Long parentCommentEntityId, Pageable pageable);

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

    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedDateTime = NOW() " +
            "where ce.id = :commentId")
    void softDeleteById(@Param("commentId") Long commentId);
}
