package world.trecord.web.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentResponse;
import world.trecord.web.service.comment.response.CommentUpdateResponse;
import world.trecord.web.service.comment.response.UserCommentsResponse;
import world.trecord.web.service.sse.SseEmitterService;

import java.util.List;
import java.util.Optional;

import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.web.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public void createComment(Long userFromId, CommentCreateRequest request) {
        UserEntity userEntity = getUserOrException(userFromId);

        RecordEntity recordEntity = getRecordOrException(request.getRecordId());

        CommentEntity parentCommentEntity = getCommentOrNull(request.getParentId());

        CommentEntity commentEntity = commentRepository.save(request.toEntity(userEntity, recordEntity, parentCommentEntity, request.getContent()));

        Long userToId = commentEntity.getRecordEntity().getFeedEntity().getUserEntity().getId();

        sseEmitterService.send(userToId, userFromId, COMMENT, buildNotificationArgs(commentEntity, userEntity));
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        CommentEntity commentEntity = getCommentWithUserOrException(commentId);

        checkPermissionOverComment(commentEntity, userId);

        commentEntity.update(request.toUpdateEntity());

        commentRepository.saveAndFlush(commentEntity);

        return CommentUpdateResponse.builder()
                .commentEntity(commentEntity)
                .build();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        CommentEntity commentEntity = getCommentWithChildCommentsOrException(commentId);

        checkPermissionOverComment(commentEntity, userId);

        commentRepository.deleteAllByCommentEntityId(commentId);

        commentRepository.softDeleteById(commentId);
    }

    public Page<CommentResponse> getReplies(Long commentId, Long viewerId, Pageable pageable) {
        CommentEntity parentComment = getCommentOrException(commentId);

        return commentRepository.findByParentCommentEntityId(parentComment.getId(), pageable)
                .map(it -> CommentResponse.builder()
                        .commentEntity(it)
                        .viewerId(viewerId)
                        .build());
    }

    public UserCommentsResponse getUserComments(Long userId) {
        UserEntity userEntity = getUserOrException(userId);

        List<CommentRecordProjection> projectionList = commentRepository.findByUserEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        return UserCommentsResponse.builder()
                .projectionList(projectionList)
                .build();
    }

    private UserEntity getUserOrException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private RecordEntity getRecordOrException(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private CommentEntity getCommentOrException(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
    }

    private CommentEntity getCommentOrNull(Long parentId) {
        return Optional.ofNullable(parentId)
                .map(this::getCommentWithUserOrException)
                .orElse(null);
    }

    private CommentEntity getCommentWithChildCommentsOrException(Long commentId) {
        return commentRepository.findWithChildCommentEntitiesById(commentId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
    }

    private CommentEntity getCommentWithUserOrException(Long commentId) {
        return commentRepository.findWithUserEntityById(commentId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
    }

    private void checkPermissionOverComment(CommentEntity commentEntity, Long userId) {
        if (!commentEntity.isCommenter(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private NotificationArgs buildNotificationArgs(CommentEntity commentEntity, UserEntity userEntity) {
        return NotificationArgs.builder()
                .commentEntity(commentEntity)
                .recordEntity(commentEntity.getRecordEntity())
                .userFromEntity(userEntity)
                .build();
    }
}
