package world.trecord.web.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.domain.notification.NotificationType;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.notification.NotificationService;
import world.trecord.web.service.notification.response.CheckNewNotificationResponse;
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

    @GetMapping("/check")
    public ApiResponse<CheckNewNotificationResponse> checkNewNotification(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.checkUnreadNotifications(userContext.getId()));
    }

    @GetMapping("/subscribe")
    public SseEmitter connectNotification(@CurrentUser UserContext userContext) {
        return sseEmitterService.connect(userContext.getId(), new SseEmitter(Duration.ofMinutes(30).toMillis()));
    }

    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                  @CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotifications(userContext.getId()));
    }

    @GetMapping("/{type}")
    public ApiResponse<NotificationListResponse> getNotificationsByType(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                        @PathVariable("type") NotificationType type,
                                                                        @CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotificationsByType(userContext.getId(), type));
    }
}
