package world.trecord.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.notification.enumeration.NotificationStatus;
import world.trecord.domain.notification.enumeration.NotificationType;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByUsersToEntityIdAndStatus(Long userId, NotificationStatus status);

    List<NotificationEntity> findByUsersToEntityIdOrderByCreatedDateTimeDesc(Long userToEntityId);

    List<NotificationEntity> findByUsersToEntityIdAndTypeOrderByCreatedDateTimeDesc(Long userToEntityId, NotificationType type);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationEntity ne " +
            "SET ne.status = :newStatus " +
            "WHERE ne.usersToEntity.id = :userId AND ne.status = :oldStatus")
    int updateNotificationStatusByUserId(@Param("userId") Long userId,
                                         @Param("oldStatus") NotificationStatus oldStatus,
                                         @Param("newStatus") NotificationStatus newStatus);

    @Transactional
    @Modifying
    @Query(value = "UPDATE notification " +
            "SET deleted_date_time = NOW() " +
            "WHERE JSON_EXTRACT(args, '$.record.id') = :recordId", nativeQuery = true)
    void deleteAllByRecordEntityId(@Param("recordId") Long recordId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE notification " +
            "SET deleted_date_time = NOW() " +
            "WHERE JSON_EXTRACT(args, '$.feed.id') = :feedId", nativeQuery = true)
    void deleteAllByFeedEntityId(@Param("feedId") Long feedId);
}
