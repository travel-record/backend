package world.trecord.domain.notification.enumeration;

import world.trecord.domain.notification.NotificationEntity;

public enum NotificationType {

    COMMENT {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getComment().getContent();
        }
    },
    RECORD_LIKE {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getUserFrom().getNickname() + "님이 회원님의 기록을 좋아합니다.";
        }
    },
    FEED_INVITATION {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getUserFrom().getNickname() + "님이 피드에 초대했어요.";
        }
    };


    public String getContent(NotificationEntity notificationEntity) {
        return "";
    }
}
