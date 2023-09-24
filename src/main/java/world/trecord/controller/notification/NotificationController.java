package world.trecord.controller.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import world.trecord.config.security.CurrentContext;
import world.trecord.config.security.UserContext;
import world.trecord.controller.ApiResponse;
import world.trecord.domain.notification.enumeration.NotificationType;
import world.trecord.dto.notification.response.CheckNewNotificationResponse;
import world.trecord.dto.notification.response.NotificationResponse;
import world.trecord.event.sse.SseEmitterService;
import world.trecord.service.notification.NotificationService;

import java.time.Duration;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                                    @CurrentContext UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotifications(userContext.getId(), pageable));
    }

    @GetMapping("/check")
    public ApiResponse<CheckNewNotificationResponse> checkNewNotification(@CurrentContext UserContext userContext) {
        return ApiResponse.ok(notificationService.checkUnreadNotifications(userContext.getId()));
    }

    @GetMapping(value = "/subscribe", produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectNotification(@CurrentContext UserContext userContext) {
        return sseEmitterService.connect(userContext.getId(), new SseEmitter(Duration.ofHours(3).toMillis()));
    }

    @GetMapping("/type/{type}")
    public ApiResponse<Page<NotificationResponse>> getNotificationsByType(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                                          @PathVariable NotificationType type,
                                                                          @CurrentContext UserContext userContext) {
        return ApiResponse.ok(notificationService.getNotificationsByType(userContext.getId(), type, pageable));
    }

    @DeleteMapping("/{notificationId}")
    public ApiResponse<Void> deleteNotification(@PathVariable Long notificationId,
                                                @CurrentContext UserContext userContext) {
        notificationService.deleteNotification(userContext.getId(), notificationId);
        return ApiResponse.ok();
    }
}
