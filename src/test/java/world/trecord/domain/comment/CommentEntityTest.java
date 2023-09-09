package world.trecord.domain.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import world.trecord.domain.users.UserEntity;

class CommentEntityTest {

    @Test
    @DisplayName("댓글의 필드값을 수정하면 수정된 값을 가진다")
    void updateTest() throws Exception {
        //given
        CommentEntity commentEntity = CommentEntity.builder()
                .content("before content")
                .build();

        String updatedContent = "updated content";
        CommentEntity updateEntity = CommentEntity.builder()
                .content(updatedContent)
                .build();

        //when
        commentEntity.update(updateEntity);

        //then
        Assertions.assertThat(commentEntity.getContent()).isEqualTo(updatedContent);
    }


    @Test
    @DisplayName("댓글 작성자이면 true를 반환한다")
    void isAuthorReturnsTrueTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity, "id", 1L);

        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity)
                .build();

        //when
        boolean result = commentEntity.isCommenter(userEntity.getId());

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("댓글 작성자가 아니면 false를 반환한다")
    void isAuthorReturnsFalseTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity1, "id", 1L);

        UserEntity userEntity2 = UserEntity.builder().build();
        ReflectionTestUtils.setField(userEntity2, "id", 2L);

        CommentEntity commentEntity = CommentEntity.builder()
                .userEntity(userEntity2)
                .build();

        //when
        boolean result = commentEntity.isCommenter(userEntity1.getId());

        //then
        Assertions.assertThat(result).isFalse();
    }
}