package world.trecord.web.service.notification.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CheckNewNotificationResponse {
    private boolean hasNewNotification;

    @Builder
    private CheckNewNotificationResponse(boolean hasNewNotification) {
        this.hasNewNotification = hasNewNotification;
    }
}

