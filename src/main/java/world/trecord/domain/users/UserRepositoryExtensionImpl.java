package world.trecord.domain.users;

import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Optional;

public class UserRepositoryExtensionImpl extends QuerydslRepositorySupport implements UserRepositoryExtension {

    public UserRepositoryExtensionImpl() {
        super(UserEntity.class);
    }

    @Override
    public Optional<UserEntity> findByKeyword(String keyword) {
        QUserEntity userEntity = QUserEntity.userEntity;

        JPQLQuery<UserEntity> query = from(userEntity)
                .where(userEntity.nickname.eq(keyword))
                .distinct();

        UserEntity result = query.fetchOne();

        return Optional.ofNullable(result);
    }
}
