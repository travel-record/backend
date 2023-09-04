package world.trecord.service.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.service.users.request.UserUpdateRequest;
import world.trecord.service.users.response.UserInfoResponse;
import world.trecord.exception.CustomException;

import java.util.Objects;

import static world.trecord.exception.CustomExceptionError.NICKNAME_DUPLICATED;
import static world.trecord.exception.CustomExceptionError.USER_NOT_FOUND;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity createNewUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .build());
    }

    public UserInfoResponse getUser(Long userId) {
        UserEntity userEntity = findUserOrException(userId);

        return UserInfoResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    @Transactional
    public UserInfoResponse updateUser(Long userId, UserUpdateRequest updateRequest) {
        UserEntity userEntity = findUserOrException(userId);

        if (isNicknameUpdated(userEntity.getNickname(), updateRequest.getNickname()) && isNicknameAlreadyInUse(updateRequest.getNickname())) {
            throw new CustomException(NICKNAME_DUPLICATED);
        }

        try {
            userEntity.update(updateRequest.toUpdateEntity());
            userRepository.saveAndFlush(userEntity);
        } catch (DataIntegrityViolationException ex) {
            if (isNicknameConstraintViolation(ex)) {
                throw new CustomException(NICKNAME_DUPLICATED);
            }
            throw ex;
        }

        return UserInfoResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    public UserContext getUserContextOrException(Long userId) throws UsernameNotFoundException {
        return userRepository.findById(userId)
                .map(UserContext::fromEntity)
                .orElseThrow(() -> {
                    log.warn("Error in method [getUserContextOrException] - User not found with ID: {}", userId);
                    return new UsernameNotFoundException(USER_NOT_FOUND.name());
                });
    }

    private UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private boolean isNicknameUpdated(String originalNickname, String updatedNickname) {
        return !Objects.equals(originalNickname, updatedNickname);
    }

    private boolean isNicknameAlreadyInUse(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private boolean isNicknameConstraintViolation(DataIntegrityViolationException ex) {
        return ex.getMessage().contains("uk_users_nickname");
    }
}