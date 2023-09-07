package world.trecord.domain.users;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class UserRepositoryExtensionImpl extends QuerydslRepositorySupport implements UserRepositoryExtension {

    public UserRepositoryExtensionImpl() {
        super(UserEntity.class);
    }

    @Override
    public Page<UserEntity> findByKeyword(String keyword, Pageable pageable) {
        QUserEntity userEntity = QUserEntity.userEntity;

        JPQLQuery<UserEntity> query = from(userEntity)
                .where(userEntity.nickname.startsWithIgnoreCase(keyword))
                .distinct();

        JPQLQuery<UserEntity> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<UserEntity> fetchResults = pageableQuery.fetchResults();

        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }
}
