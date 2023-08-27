package world.trecord.web.service.userrecordlike;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.notification.NotificationService;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeResponse;

import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_RECORD;
import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_USER;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserRecordLikeService {

    private final UserRecordLikeRepository userRecordLikeRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public UserRecordLikeResponse toggleLike(Long recordId, Long userId) {
        UserEntity userEntity = findUserEntityById(userId);

        RecordEntity recordEntity = findRecordEntityById(recordId);

        return userRecordLikeRepository.findUserRecordLikeEntityByUserEntityAndRecordEntity(userEntity, recordEntity)
                .map(this::unlike)
                .orElseGet(() -> like(userEntity, recordEntity));
    }

    private UserRecordLikeResponse unlike(UserRecordLikeEntity userRecordLikeEntity) {
        userRecordLikeRepository.delete(userRecordLikeEntity);
        return createUserRecordLikeResponse(false);
    }

    private UserRecordLikeResponse like(UserEntity userEntity, RecordEntity recordEntity) {
        saveUserRecordLikeEntity(userEntity, recordEntity);

        // TODO async 처리
        notificationService.createRecordLikeNotification(recordEntity, userEntity);

        return createUserRecordLikeResponse(true);
    }

    private void saveUserRecordLikeEntity(UserEntity userEntity, RecordEntity recordEntity) {
        UserRecordLikeEntity userRecordLikeEntity = UserRecordLikeEntity.builder()
                .userEntity(userEntity)
                .recordEntity(recordEntity)
                .build();

        userRecordLikeRepository.save(userRecordLikeEntity);
    }

    private UserRecordLikeResponse createUserRecordLikeResponse(boolean liked) {
        return UserRecordLikeResponse.builder()
                .liked(liked)
                .build();
    }

    private RecordEntity findRecordEntityById(Long recordId) {
        return recordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(NOT_EXISTING_RECORD));
    }

    private UserEntity findUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }
}
