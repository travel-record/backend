package world.trecord.web.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.trecord.domain.notification.NotificationType;
import world.trecord.domain.users.UserEntity;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.notification.NotificationService;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/check")
    public ApiResponse<CheckNewNotificationResponse> checkNewNotification(@CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(notificationService.checkNewNotification(userEntity.getId()));
    }

    // TODO add pageable
    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(@CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(notificationService.getNotifications(userEntity.getId()));
    }

    // TODO add pageable
    @GetMapping("/{type}")
    public ApiResponse<NotificationListResponse> getNotificationsByType(@PathVariable("type") NotificationType type, @CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(notificationService.getNotificationsOrException(userEntity.getId(), type));
    }
}
