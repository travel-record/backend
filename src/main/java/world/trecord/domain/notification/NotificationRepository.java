package world.trecord.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    boolean existsByUsersToEntityIdAndStatus(Long userId, NotificationStatus status);

    @Query("SELECT n " +
            "FROM NotificationEntity n " +
            "JOIN FETCH n.usersToEntity u " +
            "WHERE u.id = :userId " +
            "ORDER BY n.createdDateTime DESC")
    List<NotificationEntity> findByUsersToEntityIdOrderByCreatedDateTimeDesc(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationEntity n " +
            "SET n.status = :newStatus " +
            "WHERE n.usersToEntity.id = :userId AND n.status = :oldStatus")
    int updateNotificationStatusByUserId(@Param("userId") Long userId,
                                         @Param("oldStatus") NotificationStatus oldStatus,
                                         @Param("newStatus") NotificationStatus newStatus);
}
