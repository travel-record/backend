package world.trecord.web.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.notification.NotificationType;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.notification.NotificationService;
import world.trecord.web.service.notification.response.NotificationListResponse;
import world.trecord.web.service.sse.SseEmitterService;
import world.trecord.web.service.users.UserContext;

import java.time.Duration;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @GetMapping("/subscribe")
    public SseEmitter connectNotification(@CurrentUser UserContext userContext) {
        return sseEmitterService.connect(userContext.getId(), new SseEmitter(Duration.ofDays(1).toMillis()));
    }

    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotifications(userContext.getId()));
    }

    @GetMapping("/{type}")
    public ApiResponse<NotificationListResponse> getNotificationsByType(@PathVariable("type") NotificationType type, @CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotificationsOrException(userContext.getId(), type));
    }
}
