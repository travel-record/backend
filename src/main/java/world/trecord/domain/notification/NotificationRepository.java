package world.trecord.domain.notification;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByUsersToEntityIdAndStatus(Long userId, NotificationStatus status);

    @EntityGraph(attributePaths = {"usersToEntity", "commentEntity"})
    List<NotificationEntity> findByUsersToEntityOrderByCreatedDateTimeDesc(UserEntity userToEntity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationEntity n " +
            "SET n.status = :newStatus " +
            "WHERE n.usersToEntity.id = :userId AND n.status = :oldStatus")
    int updateNotificationStatusByUserId(@Param("userId") Long userId,
                                         @Param("oldStatus") NotificationStatus oldStatus,
                                         @Param("newStatus") NotificationStatus newStatus);

    List<NotificationEntity> findByUsersToEntityAndTypeOrderByCreatedDateTimeDesc(UserEntity userToEntity, NotificationType type);

    @Modifying
    @Query("UPDATE NotificationEntity ne " +
            "SET ne.deletedDateTime = NOW() " +
            "where ne.recordEntity = :recordEntity")
    void deleteAllByRecordEntity(@Param("recordEntity") RecordEntity recordEntity);
}
