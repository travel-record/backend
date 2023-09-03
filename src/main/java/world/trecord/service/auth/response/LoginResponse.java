package world.trecord.service.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class LoginResponse {

    private User user;
    private Token token;

    @Builder
    public LoginResponse(Long userId, String nickname, String token, String refreshToken) {
        this.user = new User(userId, nickname);
        this.token = new Token(token, refreshToken);
    }

    @AllArgsConstructor
    @Getter
    public static class User {
        private Long userId;
        private String nickname;
    }

    @AllArgsConstructor
    @Getter
    public static class Token {
        private String token;
        private String refreshToken;
    }

}
