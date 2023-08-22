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
import world.trecord.web.service.comment.request.CommentDeleteRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentCreateResponse;
import world.trecord.web.service.comment.response.CommentDeleteResponse;
import world.trecord.web.service.comment.response.CommentUpdateResponse;
import world.trecord.web.service.notification.NotificationService;

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
    public CommentCreateResponse createComment(Long userId, CommentCreateRequest request) {

        UserEntity userEntity = findUserEntityBy(userId);

        RecordEntity recordEntity = findRecordEntityBy(request.getRecordId());

        CommentEntity commentEntity = commentRepository.save(request.toEntity(userEntity, recordEntity, request.getContent()));

        notificationService.createCommentNotification(commentEntity);

        return CommentCreateResponse.builder()
                .recordEntity(recordEntity)
                .commentEntity(commentEntity)
                .build();
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long userId, CommentUpdateRequest request) {

        UserEntity userEntity = findUserEntityBy(userId);

        CommentEntity commentEntity = findCommentEntityWithUserEntityBy(request.getCommentId());

        checkPermissionOverComment(userEntity, commentEntity);

        commentEntity.update(request.getContent());

        return CommentUpdateResponse.builder()
                .commentEntity(commentEntity)
                .build();
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long userId, CommentDeleteRequest request) {

        UserEntity userEntity = findUserEntityBy(userId);

        CommentEntity commentEntity = findCommentEntityWithUserEntityBy(request.getCommentId());

        checkPermissionOverComment(userEntity, commentEntity);

        commentRepository.delete(commentEntity);

        return CommentDeleteResponse.builder()
                .commentEntity(commentEntity)
                .build();
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

    private void checkPermissionOverComment(UserEntity userEntity, CommentEntity commentEntity) {
        if (!userEntity.isCommenterOf(commentEntity)) {
            throw new CustomException(FORBIDDEN);
        }
    }
}
