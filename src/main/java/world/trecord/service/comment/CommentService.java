package world.trecord.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.comment.request.CommentCreateRequest;
import world.trecord.dto.comment.request.CommentUpdateRequest;
import world.trecord.dto.comment.response.CommentResponse;
import world.trecord.dto.comment.response.UserCommentResponse;
import world.trecord.event.notification.NotificationEvent;
import world.trecord.exception.CustomException;
import world.trecord.service.record.RecordService;
import world.trecord.service.users.UserService;

import java.util.Optional;

import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;
import static world.trecord.exception.CustomExceptionError.COMMENT_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.FORBIDDEN;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final UserService userService;
    private final RecordService recordService;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createComment(Long userFromId, CommentCreateRequest request) {
        UserEntity userEntity = userService.findUserOrException(userFromId);
        RecordEntity recordEntity = recordService.findRecordOrException(request.getRecordId());
        Optional<CommentEntity> parentOptional = findCommentOrOptional(request.getParentId());
        if (request.getParentId() != null && parentOptional.isEmpty()) {
            throw new CustomException(COMMENT_NOT_FOUND);
        }

        CommentEntity parentCommentEntity = parentOptional.orElse(null);
        CommentEntity commentEntity = commentRepository.save(request.toEntity(userEntity, recordEntity, parentCommentEntity, request.getContent()));
        Long userToId = commentEntity.getRecordEntity().getFeedEntity().getUserEntity().getId();

        eventPublisher.publishEvent(new NotificationEvent(userToId, userFromId, COMMENT, buildNotificationArgs(recordEntity, commentEntity, userEntity)));
    }

    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        CommentEntity commentEntity = findCommentOrException(commentId);
        ensureUserHasPermissionOverComment(commentEntity, userId);

        commentEntity.update(request.toUpdateEntity());
        commentRepository.saveAndFlush(commentEntity);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        CommentEntity commentEntity = findCommentOrException(commentId);
        ensureUserHasPermissionOverComment(commentEntity, userId);

        commentRepository.deleteAllByCommentEntityId(commentId);
        commentRepository.delete(commentEntity);
    }

    public Page<CommentResponse> getReplies(Long userId, Long commentId, Pageable pageable) {
        CommentEntity parentComment = findCommentOrException(commentId);
        return commentRepository.findWithUserEntityByParentCommentEntityId(parentComment.getId(), pageable)
                .map(it -> CommentResponse.of(it.getUserEntity(), it, userId));
    }

    public Page<UserCommentResponse> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable).map(UserCommentResponse::of);
    }

    private CommentEntity findCommentOrException(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(COMMENT_NOT_FOUND));
    }

    private Optional<CommentEntity> findCommentOrOptional(Long parentId) {
        if (parentId == null) {
            return Optional.empty();
        }
        return commentRepository.findById(parentId);
    }

    private void ensureUserHasPermissionOverComment(CommentEntity commentEntity, Long userId) {
        if (!commentEntity.isCommenter(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }

    private NotificationArgs buildNotificationArgs(RecordEntity recordEntity, CommentEntity commentEntity, UserEntity userEntity) {
        return NotificationArgs.builder()
                .recordEntity(recordEntity)
                .commentEntity(commentEntity)
                .userFromEntity(userEntity)
                .build();
    }
}
