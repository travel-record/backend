package world.trecord.web.service.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.exception.CustomException;
import world.trecord.web.service.users.request.UserUpdateRequest;
import world.trecord.web.service.users.response.UserInfoResponse;

import java.util.List;

import static world.trecord.web.exception.CustomExceptionError.EXISTING_NICKNAME;
import static world.trecord.web.exception.CustomExceptionError.NOT_EXISTING_USER;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional
    public UserEntity createNewUserWith(String email) {
        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .build();

        return userRepository.save(userEntity);
    }

    public UserInfoResponse findUserBy(Long userId) {
        UserEntity userEntity = findUserEntityBy(userId);

        return UserInfoResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest updateRequest) {
        UserEntity userEntity = findUserEntityBy(userId);

        if (isNicknameChangedAndDuplicate(updateRequest.getNickname(), userEntity.getNickname())) {
            throw new CustomException(EXISTING_NICKNAME);
        }

        userEntity.update(updateRequest.getNickname(), updateRequest.getImageUrl(), updateRequest.getIntroduction());

        return UserInfoResponse.builder()
                .userEntity(userEntity)
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new UsernameNotFoundException("NOT_EXIST_USER"));
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        return new User(userEntity.getId().toString(), "", grantedAuthorities);
    }

    private UserEntity findUserEntityBy(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(NOT_EXISTING_USER));
    }

    private boolean isNicknameChangedAndDuplicate(String requestNickname, String originNickname) {
        return (originNickname != null) && (!originNickname.equals(requestNickname)) && (userRepository.existsByNickname(requestNickname));
    }
}
