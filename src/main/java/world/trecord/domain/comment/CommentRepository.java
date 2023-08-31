package world.trecord.domain.comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @EntityGraph(attributePaths = "userEntity")
    Optional<CommentEntity> findCommentEntityWithUserEntityById(@Param("id") Long commentId);

    @EntityGraph(attributePaths = "childCommentEntities")
    Optional<CommentEntity> findCommentEntityWithChildCommentEntitiesById(@Param("id") Long commentId);

    @Query("SELECT re.id as recordId, re.title as recordTitle, ce.id as commentId, ce.content as content " +
            "FROM CommentEntity ce " +
            "JOIN ce.recordEntity re " +
            "WHERE ce.userEntity = :userEntity " +
            "ORDER BY ce.createdDateTime DESC")
    List<CommentRecordProjection> findByUserEntityOrderByCreatedDateTimeDesc(@Param("userEntity") UserEntity userEntity);

    @EntityGraph(attributePaths = "userEntity")
    List<CommentEntity> findCommentEntityWithUserEntityByRecordEntityOrderByCreatedDateTimeAsc(RecordEntity recordEntity);

    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedDateTime = NOW() " +
            "where ce.recordEntity.id = :recordId")
    void deleteAllByRecordEntityId(@Param("recordId") Long recordId);

    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedDateTime = NOW() " +
            "where ce.parentCommentEntity = :commentEntity")
    void deleteAllByCommentEntity(@Param("commentEntity") CommentEntity commentEntity);

    @Modifying
    @Query("UPDATE CommentEntity ce " +
            "SET ce.deletedDateTime = NOW() " +
            "where ce = :commentEntity")
    void softDelete(@Param("commentEntity") CommentEntity commentEntity);
}
