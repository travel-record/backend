package world.trecord.domain.notification;

public enum NotificationType {

    COMMENT {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getCommentContent();
        }
    },
    RECORD_LIKE {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getUserFromNickname() + "님이 회원님의 기록을 좋아합니다.";
        }
    },
    MENTION,
    SHARE,
    SYSTEM_NOTIFICATION,
    EVENT_INVITATION,
    FRIEND_REQUEST;

    public String getContent(NotificationEntity notificationEntity) {
        return "";
    }
}
