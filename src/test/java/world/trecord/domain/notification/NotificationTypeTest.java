package world.trecord.domain.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.notification.args.NotificationArgs;
import world.trecord.domain.users.UserEntity;

import static org.mockito.Mockito.when;
import static world.trecord.domain.notification.enumeration.NotificationType.COMMENT;
import static world.trecord.domain.notification.enumeration.NotificationType.RECORD_LIKE;

@ExtendWith(MockitoExtension.class)
class NotificationTypeTest {


    @Mock
    private NotificationEntity mockNotificationEntity;

    @Mock
    private UserEntity mockUserFromEntity;

    @Mock
    private CommentEntity mockCommentEntity;

    @Test
    @DisplayName("type이 COMMENT이면 comment의 내용을 반환한다")
    void getContentWhenTypeIsCommentTest() {
        //given
        String expectedContent = "test comment content";

        CommentEntity commentEntity = CommentEntity
                .builder()
                .content(expectedContent)
                .build();

        NotificationArgs mockArgs = NotificationArgs.builder()
                .commentEntity(commentEntity)
                .build();

        when(mockNotificationEntity.getArgs()).thenReturn(mockArgs);

        //when
        String content = COMMENT.getContent(mockNotificationEntity);

        //then
        Assertions.assertThat(content).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("type이 RECORD_LIKE이면 좋아요한 사용자의 닉네임을 포함한 메시지를 반환한다")
    void getContentWhenTypeIsRecordLike() {
        //given
        String expectedNickname = "user nickname";

        UserEntity userFromEntity = UserEntity.builder()
                .nickname(expectedNickname)
                .build();

        NotificationArgs mockArgs = NotificationArgs.builder()
                .userFromEntity(userFromEntity)
                .build();

        when(mockNotificationEntity.getArgs()).thenReturn(mockArgs);

        //when
        String content = RECORD_LIKE.getContent(mockNotificationEntity);

        //then
        Assertions.assertThat(content).isEqualTo(expectedNickname + "님이 회원님의 기록을 좋아합니다.");
    }

}