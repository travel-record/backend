package world.trecord.web.service.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.userrecordlike.projection.UserRecordProjection;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.users.request.UserUpdateRequest;
import world.trecord.web.service.users.response.UserCommentsResponse;
import world.trecord.web.service.users.response.UserInfoResponse;
import world.trecord.web.service.users.response.UserRecordLikeListResponse;

import java.util.List;
import java.util.Objects;

import static world.trecord.web.exception.CustomExceptionError.NICKNAME_DUPLICATED;
import static world.trecord.web.exception.CustomExceptionError.USER_NOT_FOUND;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final UserRecordLikeRepository userRecordLikeRepository;

    @Transactional
    public UserEntity createNewUser(String email) {
        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .build();

        return userRepository.save(userEntity);
    }

    public UserInfoResponse getUser(Long userId) {
        UserEntity userEntity = getUserOrException(userId);

        return UserInfoResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    @Transactional
    public UserInfoResponse updateUser(Long userId, UserUpdateRequest updateRequest) {
        UserEntity userEntity = getUserOrException(userId);

        if (isNicknameUpdatedAndExists(userEntity.getNickname(), updateRequest.getNickname())) {
            throw new CustomException(NICKNAME_DUPLICATED);
        }

        userEntity.update(updateRequest.toUpdateEntity());

        return UserInfoResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    public UserCommentsResponse getUserComments(Long userId) {
        UserEntity userEntity = getUserOrException(userId);

        List<CommentRecordProjection> projectionList = commentRepository.findByUserEntityIdOrderByCreatedDateTimeDesc(userEntity.getId());

        return UserCommentsResponse.builder()
                .projectionList(projectionList)
                .build();
    }

    public UserRecordLikeListResponse getUserRecordLikeList(Long userId) {
        UserEntity userEntity = getUserOrException(userId);

        List<UserRecordProjection> projectionList = userRecordLikeRepository.findLikeRecordsByUserEntityId(userEntity.getId());

        return UserRecordLikeListResponse.builder()
                .projectionList(projectionList)
                .build();
    }

    public UserContext getUserContextOrException(Long userId) throws UsernameNotFoundException {
        return userRepository.findById(userId)
                .map(UserContext::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND.name()));
    }

    private UserEntity getUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private boolean isNicknameUpdatedAndExists(String originalNickname, String updatedNickname) {
        // TODO 동시성 처리
        return !Objects.equals(originalNickname, updatedNickname) && (userRepository.existsByNickname(updatedNickname));
    }
}
