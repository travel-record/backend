package world.trecord.service.userrecordlike;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.notification.NotificationArgs;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.service.userrecordlike.response.UserRecordLikeListResponse;
import world.trecord.service.userrecordlike.response.UserRecordLikeResponse;
import world.trecord.exception.CustomException;
import world.trecord.service.sse.SseEmitterService;

import java.util.List;

import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;
import static world.trecord.exception.CustomExceptionError.RECORD_NOT_FOUND;
import static world.trecord.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserRecordLikeService {

    private final UserRecordLikeRepository userRecordLikeRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public UserRecordLikeResponse toggleLike(Long userId, Long recordId) {
        UserEntity userEntity = findUserOrException(userId);
        RecordEntity recordEntity = findRecordOrException(recordId);

        return userRecordLikeRepository.findByUserEntityIdAndRecordEntityId(userEntity.getId(), recordEntity.getId())
                .map(this::unlike)
                .orElseGet(() -> like(userEntity, recordEntity));
    }

    public UserRecordLikeListResponse getUserRecordLikeList(Long userId) {
        UserEntity userEntity = findUserOrException(userId);

        List<UserRecordProjection> projectionList = userRecordLikeRepository.findLikeRecordsByUserEntityId(userEntity.getId());

        return UserRecordLikeListResponse.builder()
                .projectionList(projectionList)
                .build();
    }

    private UserRecordLikeResponse unlike(UserRecordLikeEntity userRecordLikeEntity) {
        userRecordLikeRepository.softDeleteById(userRecordLikeEntity.getId());
        return doBuildLikeResponse(false);
    }

    private UserRecordLikeResponse like(UserEntity userEntity, RecordEntity recordEntity) {
        saveRecordLike(userEntity, recordEntity);
        Long userToId = recordEntity.getFeedEntity().getUserEntity().getId();
        sseEmitterService.send(userToId, userEntity.getId(), RECORD_LIKE, doBuildNotificationArgs(userEntity, recordEntity));
        return doBuildLikeResponse(true);
    }

    private void saveRecordLike(UserEntity userEntity, RecordEntity recordEntity) {
        UserRecordLikeEntity userRecordLikeEntity = UserRecordLikeEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();

        userRecordLikeRepository.save(userRecordLikeEntity);
    }

    private UserRecordLikeResponse doBuildLikeResponse(boolean liked) {
        return UserRecordLikeResponse.builder()
                .liked(liked)
                .build();
    }

    private RecordEntity findRecordOrException(Long recordId) {
        return recordRepository.findById(recordId).orElseThrow(() -> new CustomException(RECORD_NOT_FOUND));
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private NotificationArgs doBuildNotificationArgs(UserEntity userEntity, RecordEntity recordEntity) {
        return NotificationArgs.builder()
                .recordEntity(recordEntity)
                .userFromEntity(userEntity)
                .build();
    }
}
