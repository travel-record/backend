package world.trecord.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.config.security.CurrentUser;
import world.trecord.controller.ApiResponse;
import world.trecord.domain.notification.NotificationType;
import world.trecord.service.notification.NotificationService;
import world.trecord.service.notification.response.CheckNewNotificationResponse;
import world.trecord.service.notification.response.NotificationListResponse;
import world.trecord.service.sse.SseEmitterService;
import world.trecord.service.users.UserContext;

import java.time.Duration;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

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

    @GetMapping(value = "/subscribe", produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectNotification(@CurrentUser UserContext userContext) {
        return sseEmitterService.connect(userContext.getId(), new SseEmitter(Duration.ofHours(3).toMillis()));
    }

    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                  @CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotifications(userContext.getId()));
    }

    @GetMapping("/type/{type}")
    public ApiResponse<NotificationListResponse> getNotificationsByType(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                        @PathVariable("type") NotificationType type,
                                                                        @CurrentUser UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotificationsByType(userContext.getId(), type));
    }
}
