package world.trecord.domain.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;

class UserEntityTest {

    @Test
    @DisplayName("사용자 정보 업데이트 테스트")
    void updateTest() throws Exception {
        //given
        String updatedNickname = "after nickname";
        String updatedIntroduction = "after introduction";
        String updatedImageUrl = "after image url";

        UserEntity userEntity = UserEntity.builder()
                .nickname("before nickname")
                .introduction("before introduction")
                .imageUrl("before image url")
                .build();

        UserEntity updateEntity = UserEntity.builder()
                .nickname(updatedNickname)
                .introduction(updatedIntroduction)
                .imageUrl(updatedImageUrl)
                .build();

        //when
        userEntity.update(updateEntity);

        //then
        Assertions.assertThat(userEntity)
                .extracting("nickname", "imageUrl", "introduction")
                .containsExactly(updatedNickname, updatedImageUrl, updatedIntroduction);
    }

    @Test
    @DisplayName("사용자가 피드 매니저이면 true를 반환한다")
    void isManagerOfFeedWhenUserIsManagerOfFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .build();

        FeedEntity feedEntity = FeedEntity.builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = userEntity.isManagerOf(feedEntity);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 피드 매니저가 아니면 false를 반환한다")
    void isManagerOfFeedWhenUserIsNotManagerOfFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .build();

        UserEntity otherEntity = UserEntity.builder()
                .id(2L)
                .build();

        FeedEntity feedEntity = FeedEntity.builder()
                .userEntity(otherEntity)
                .build();

        //when
        boolean result = userEntity.isManagerOf(feedEntity);

        //then
        Assertions.assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자가 댓글 작성자이면 true를 반환한다")
    void isCommenterOfCommentWhenUserCommentsTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .build();

        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = userEntity.isCommenterOf(commentEntity);

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("사용자가 댓글 작성자가 아니면 false를 반환한다")
    void isCommenterOfCommentWhenUserNotCommentsTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .id(1L)
                .build();

        UserEntity otherEntity = UserEntity.builder()
                .id(2L)
                .build();

        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(otherEntity)
                .build();

        //when
        boolean result = userEntity.isCommenterOf(commentEntity);

        //then
        Assertions.assertThat(result).isFalse();
    }


}