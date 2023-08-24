package world.trecord.domain.notification;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.users.UserEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static world.trecord.domain.notification.NotificationType.COMMENT;
import static world.trecord.domain.notification.NotificationType.RECORD_LIKE;

class NotificationTypeTest {

    private NotificationEntity mockNotificationEntity;
    private UserEntity mockUserFromEntity;
    private CommentEntity mockCommentEntity;

    @BeforeEach
    void setUp() {
        mockNotificationEntity = mock(NotificationEntity.class);
        mockUserFromEntity = mock(UserEntity.class);
        mockCommentEntity = mock(CommentEntity.class);
    }

    @Test
    @DisplayName("type이 COMMENT이면 comment의 내용을 반환한다")
    void getContentWhenTypeIsCommentTest() {
        //given
        String expectedContent = "Test comment content";
        when(mockNotificationEntity.getCommentEntity()).thenReturn(mockCommentEntity);
        when(mockCommentEntity.getContent()).thenReturn(expectedContent);

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
        when(mockNotificationEntity.getUsersFromEntity()).thenReturn(mockUserFromEntity);
        when(mockUserFromEntity.getNickname()).thenReturn(expectedNickname);

        //when
        String content = RECORD_LIKE.getContent(mockNotificationEntity);

        //then
        Assertions.assertThat(content).isEqualTo(expectedNickname + "님이 회원님의 기록을 좋아합니다.");
    }

}