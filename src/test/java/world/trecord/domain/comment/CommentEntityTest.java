package world.trecord.domain.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

}