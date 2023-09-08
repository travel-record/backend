package world.trecord.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.exception.CustomException;
import world.trecord.service.comment.request.CommentCreateRequest;
import world.trecord.service.comment.request.CommentUpdateRequest;
import world.trecord.service.comment.response.CommentResponse;
import world.trecord.service.comment.response.UserCommentsResponse;
import world.trecord.service.notification.NotificationEvent;

import java.util.List;
import java.util.Optional;

import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createComment(Long userFromId, CommentCreateRequest request) {
        UserEntity userEntity = findUserOrException(userFromId);
        RecordEntity recordEntity = findRecordOrException(request.getRecordId());
        Optional<CommentEntity> parentOptional = findCommentOrOptional(request.getParentId());

        if (request.getParentId() != null && parentOptional.isEmpty()) {
            throw new CustomException(COMMENT_NOT_FOUND);
        }

        CommentEntity parentCommentEntity = parentOptional.orElse(null);
        CommentEntity commentEntity = commentRepository.save(request.toEntity(userEntity, recordEntity, parentCommentEntity, request.getContent()));
        Long userToId = commentEntity.getRecordEntity().getFeedEntity().getUserEntity().getId();

        eventPublisher.publishEvent(new NotificationEvent(userToId, userFromId, COMMENT, buildNotificationArgs(commentEntity, userEntity)));
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

    public Page<CommentResponse> getReplies(Optional<Long> viewerId, Long commentId, Pageable pageable) {
        CommentEntity parentComment = findCommentOrException(commentId);

        return commentRepository.findByParentCommentEntityId(parentComment.getId(), pageable)
                .map(it -> CommentResponse.builder()
                        .commentEntity(it)
                        .viewerId(viewerId.orElse(null))
                        .build());
    }

    public UserCommentsResponse getUserComments(Long userId) {
        UserEntity userEntity = findUserOrException(userId);
        List<CommentRecordProjection> projectionList = commentRepository.findByUserEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        return UserCommentsResponse.builder()
                .projectionList(projectionList)
                .build();
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private RecordEntity findRecordOrException(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
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

    private NotificationArgs buildNotificationArgs(CommentEntity commentEntity, UserEntity userEntity) {
        return NotificationArgs.builder()
                .commentEntity(commentEntity)
                .recordEntity(commentEntity.getRecordEntity())
                .userFromEntity(userEntity)
                .build();
    }
}
