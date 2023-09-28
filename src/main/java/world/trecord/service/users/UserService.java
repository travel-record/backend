package world.trecord.service.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.redis.UserCacheRepository;
import world.trecord.config.security.account.UserContext;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.dto.users.request.UserUpdateRequest;
import world.trecord.dto.users.response.UserResponse;
import world.trecord.exception.CustomException;

import java.util.Objects;
import java.util.Optional;

import static world.trecord.exception.CustomExceptionError.NICKNAME_DUPLICATED;
import static world.trecord.exception.CustomExceptionError.USER_NOT_FOUND;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserCacheRepository userCacheRepository;

    @Transactional
    public UserEntity findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    try {
                        return saveUser(email);
                    } catch (DataIntegrityViolationException ex) {
                        return userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalStateException("Unexpected error while retrieving the user with email: " + email, ex));
                    }
                });
    }
    
    public UserEntity saveUser(String email) {
        return userRepository.save(UserEntity.builder()
                .email(email)
                .build());
    }

    public UserResponse getUser(Long userId) {
        UserEntity userEntity = findUserOrException(userId);
        return UserResponse.of(userEntity);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest updateRequest) {
        UserEntity userEntity = findUserOrException(userId);

        if (isNicknameUpdated(userEntity.getNickname(), updateRequest.getNickname()) && isNicknameAlreadyInUse(updateRequest.getNickname())) {
            throw new CustomException(NICKNAME_DUPLICATED);
        }

        try {
            userEntity.update(updateRequest.toUpdateEntity());
            userRepository.saveAndFlush(userEntity);
        } catch (DataIntegrityViolationException ex) {
            if (isNicknameAlreadyInUse(updateRequest.getNickname())) {
                throw new CustomException(NICKNAME_DUPLICATED);
            }
            throw ex;
        }

        return UserResponse.of(userEntity);
    }

    public UserContext getUserContextOrException(Long userId) {
        Optional<UserContext> userContextOptional = userCacheRepository.getUserContext(userId);
        return userContextOptional
                .orElseGet(() -> userRepository.findById(userId)
                        .map(userEntity -> userCacheRepository.setUserContext(UserContext.fromEntity(userEntity)))
                        .orElseThrow(() -> {
                            log.warn("Error in method [getUserContextOrException] - User not found with ID: {}", userId);
                            return new CustomException(USER_NOT_FOUND);
                        }));
    }

    public UserResponse searchUser(Long userId, String keyword) {
        return userRepository.findByKeyword(keyword)
                .filter(userEntity -> !Objects.equals(userId, userEntity.getId()))
                .map(UserResponse::of)
                .orElse(null);
    }

    public UserEntity findUserOrException(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private boolean isNicknameUpdated(String originalNickname, String updatedNickname) {
        return !Objects.equals(originalNickname, updatedNickname);
    }

    private boolean isNicknameAlreadyInUse(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
