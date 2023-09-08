package world.trecord.domain.notification;

public enum NotificationType {

    COMMENT {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getComment().getCommentContent();
        }
    },
    RECORD_LIKE {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getUserFrom().getUserFromNickname() + "님이 회원님의 기록을 좋아합니다.";
        }
    },
    FEED_INVITATION {
        @Override
        public String getContent(NotificationEntity notificationEntity) {
            return notificationEntity.getArgs().getUserFrom().getUserFromNickname() + "님이 피드에 초대했어요.";
        }
    };


    public String getContent(NotificationEntity notificationEntity) {
        return "";
    }
}
