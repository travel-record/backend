package world.trecord.service.userrecordlike;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.userrecordlike.response.UserRecordLikeResponse;
import world.trecord.dto.userrecordlike.response.UserRecordLikedResponse;
import world.trecord.event.notification.NotificationEvent;
import world.trecord.exception.CustomException;
import world.trecord.service.users.UserService;

import static world.trecord.domain.notification.enumeration.NotificationType.RECORD_LIKE;
import static world.trecord.exception.CustomExceptionError.RECORD_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserRecordLikeService {

    private final UserRecordLikeRepository userRecordLikeRepository;
    private final RecordRepository recordRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public UserRecordLikedResponse toggleLike(Long userId, Long recordId) {
        UserEntity userEntity = userService.findUserOrException(userId);
        RecordEntity recordEntity = findRecordWithLockOrException(recordId);

        return userRecordLikeRepository.findByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId())
                .map(this::unlike)
                .orElseGet(() -> like(userEntity, recordEntity));
    }

    public Page<UserRecordLikeResponse> getUserRecordLikeList(Long userId, Pageable pageable) {
        return userRecordLikeRepository.findLikeRecordsByUserId(userId, pageable)
                .map(UserRecordLikeResponse::of);
    }

    private UserRecordLikedResponse unlike(UserRecordLikeEntity userRecordLikeEntity) {
        userRecordLikeRepository.delete(userRecordLikeEntity);
        return buildLikeResponse(false);
    }

    private UserRecordLikedResponse like(UserEntity userEntity, RecordEntity recordEntity) {
        saveRecordLike(userEntity, recordEntity);
        Long userToId = recordEntity.getFeedEntity().getUserEntity().getId();
        eventPublisher.publishEvent(new NotificationEvent(userToId, userEntity.getId(), RECORD_LIKE, buildNotificationArgs(userEntity, recordEntity)));
        return buildLikeResponse(true);
    }

    private void saveRecordLike(UserEntity userEntity, RecordEntity recordEntity) {
        UserRecordLikeEntity userRecordLikeEntity = UserRecordLikeEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();

        userRecordLikeRepository.save(userRecordLikeEntity);
    }

    private UserRecordLikedResponse buildLikeResponse(boolean liked) {
        return UserRecordLikedResponse.builder()
                .liked(liked)
                .build();
    }

    private RecordEntity findRecordWithLockOrException(Long recordId) {
        return recordRepository.findByIdForUpdate(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private NotificationArgs buildNotificationArgs(UserEntity userEntity, RecordEntity recordEntity) {
        return NotificationArgs.builder()
                .recordEntity(recordEntity)
                .userFromEntity(userEntity)
                .build();
    }
}
