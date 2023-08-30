package world.trecord.web.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.trecord.domain.notification.NotificationType;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.notification.NotificationService;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
import world.trecord.web.service.notification.response.NotificationListResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/check")
    public ApiResponse<CheckNewNotificationResponse> checkNewNotification(@LoginUserId String userId) {
        return ApiResponse.ok(notificationService.checkNewNotification(Long.parseLong(userId)));
    }

    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(@LoginUserId String userId) {
        return ApiResponse.ok(notificationService.getNotifications(Long.parseLong(userId)));
    }

    @GetMapping("/{type}")
    public ApiResponse<NotificationListResponse> getNotificationsByType(@PathVariable("type") NotificationType type, @LoginUserId String userId) {
        return ApiResponse.ok(notificationService.getNotifications(Long.parseLong(userId), type));
    }
}
