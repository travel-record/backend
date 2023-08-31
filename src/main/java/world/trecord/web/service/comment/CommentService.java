package world.trecord.web.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentUpdateResponse;
import world.trecord.web.service.notification.NotificationService;

import java.util.Optional;

import static world.trecord.web.exception.CustomExceptionError.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final NotificationService notificationService;

    @Transactional
    public void createComment(Long userId, CommentCreateRequest request) {
        UserEntity userEntity = findUserEntityBy(userId);

        RecordEntity recordEntity = findRecordEntityBy(request.getRecordId());

        CommentEntity parentCommentEntity = findParentCommentEntity(request.getParentId());

        CommentEntity commentEntity = commentRepository.save(request.toEntity(userEntity, recordEntity, parentCommentEntity, request.getContent()));

        notificationService.createCommentNotification(commentEntity);
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long userId, Long commentId, CommentUpdateRequest request) {
        CommentEntity commentEntity = findCommentEntityWithUserEntityBy(commentId);

        checkPermissionOverComment(commentEntity, userId);

        commentEntity.update(request.toUpdateEntity());

        return CommentUpdateResponse.builder()
                .commentEntity(commentEntity)
                .build();
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        CommentEntity commentEntity = findCommentEntityWithChildCommentEntitiesWith(commentId);

        checkPermissionOverComment(commentEntity, userId);

        commentRepository.deleteAllByCommentEntity(commentEntity);

        commentRepository.softDelete(commentEntity);
    }

    private CommentEntity findParentCommentEntity(Long parentId) {
        return Optional.ofNullable(parentId)
                .map(this::findCommentEntityWithUserEntityBy)
                .orElse(null);
    }

    private CommentEntity findCommentEntityWithChildCommentEntitiesWith(Long commentId) {
        return commentRepository.findCommentEntityWithChildCommentEntitiesById(commentId).orElseThrow(() -> new CustomException(NOT_EXISTING_COMMENT));
    }

    private CommentEntity findCommentEntityWithUserEntityBy(Long commentId) {
        return commentRepository.findCommentEntityWithUserEntityById(commentId).orElseThrow(() -> new CustomException(NOT_EXISTING_COMMENT));
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private RecordEntity findRecordEntityBy(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private void checkPermissionOverComment(CommentEntity commentEntity, Long userId) {
        if (!commentEntity.isCommenter(userId)) {
            throw new CustomException(FORBIDDEN);
        }
    }
}
