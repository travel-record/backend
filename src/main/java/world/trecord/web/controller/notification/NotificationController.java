package world.trecord.web.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.trecord.domain.notification.NotificationType;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.notification.NotificationService;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;
import world.trecord.web.service.users.UserContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/check")
    public ApiResponse<CheckNewNotificationResponse> checkNewNotification(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.checkNewNotification(userContext.getId()));
    }

    // TODO add pageable
    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotifications(userContext.getId()));
    }

    // TODO add pageable
    @GetMapping("/{type}")
    public ApiResponse<NotificationListResponse> getNotificationsByType(@PathVariable("type") NotificationType type, @CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotificationsOrException(userContext.getId(), type));
    }
}
