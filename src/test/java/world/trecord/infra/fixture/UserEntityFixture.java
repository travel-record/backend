package world.trecord.infra.fixture;

import world.trecord.domain.users.UserEntity;

import java.util.UUID;

public abstract class UserEntityFixture {

    public static UserEntity of(String email, String nickname) {
        return UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }

    public static UserEntity of(String email) {
        return of(email, null);
    }

    public static UserEntity of() {
        return of(UUID.randomUUID().toString() + System.currentTimeMillis() + Thread.currentThread(), null);
    }
}
