package world.trecord.web.service.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
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
import world.trecord.web.security.UserContext;
import world.trecord.web.service.users.request.UserUpdateRequest;
import world.trecord.web.service.users.response.UserCommentsResponse;
import world.trecord.web.service.users.response.UserInfoResponse;
import world.trecord.web.service.users.response.UserRecordLikeListResponse;

import java.util.List;
import java.util.Objects;

import static world.trecord.web.exception.CustomExceptionError.EXISTING_NICKNAME;
import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_USER;

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
            throw new CustomException(EXISTING_NICKNAME);
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

    public UserContext loadUserContext(Long userId) throws UsernameNotFoundException {
        return userRepository.findById(userId)
                .map(userEntity -> new UserContext(userEntity, AuthorityUtils.createAuthorityList(userEntity.getRole())))
                .orElseThrow(() -> new UsernameNotFoundException(NOT_EXISTING_USER.name()));
    }

    private UserEntity getUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private boolean isNicknameUpdatedAndExists(String originNickname, String requestNickname) {
        return !Objects.equals(originNickname, requestNickname) && (userRepository.existsByNickname(requestNickname));
    }
}
