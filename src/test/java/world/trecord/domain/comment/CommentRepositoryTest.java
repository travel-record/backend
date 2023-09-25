package world.trecord.domain.comment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.projection.CommentRecordProjection;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.infra.fixture.CommentEntityFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.RecordEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.test.AbstractIntegrationTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@Transactional
class CommentRepositoryTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("사용자가 작성한 댓글 리스트를 등록 시간 내림차순으로 기록과 함께 조회하여 projection으로 반환한다")
    void findByUserEntityOrderByCreatedDateTimeDescTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 1);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 2);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        CommentEntity commentEntity1 = CommentEntityFixture.of(userEntity, recordEntity1);
        CommentEntity commentEntity2 = CommentEntityFixture.of(userEntity, recordEntity2);
        CommentEntity commentEntity3 = CommentEntityFixture.of(userEntity, recordEntity2);
        CommentEntity commentEntity4 = CommentEntityFixture.of(userEntity, recordEntity1);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        int pageSize = 5;
        int pageNumber = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<CommentRecordProjection> page = commentRepository.findByUserId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent())
                .hasSize(4)
                .extracting("recordId", "commentId")
                .containsOnly(
                        tuple(recordEntity1.getId(), commentEntity4.getId()),
                        tuple(recordEntity2.getId(), commentEntity3.getId()),
                        tuple(recordEntity2.getId(), commentEntity2.getId()),
                        tuple(recordEntity1.getId(), commentEntity1.getId()));
    }

    @Test
    @DisplayName("사용자가 작성한 댓글이 없으면 빈 배열을 반환한다")
    void findByUserEntityOrderByCreatedDateTimeDescWhenUserNotCommentOnRecordTest() throws Exception {
        //given
        int pageSize = 5;
        int pageNumber = 0;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        UserEntity userEntity = userRepository.save(UserEntityFixture.of());

        //when
        Page<CommentRecordProjection> page = commentRepository.findByUserId(userEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트를 댓글 작성자, 대댓글과 페이지네이션으로 조회한다")
    void findCommentEntityByRecordEntityOrderByCreatedDateTimeAsc() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 1));

        CommentEntity commentEntity1 = CommentEntityFixture.of(userEntity, recordEntity);
        CommentEntity commentEntity2 = CommentEntityFixture.of(userEntity, recordEntity);
        CommentEntity commentEntity3 = CommentEntityFixture.of(userEntity, recordEntity);
        CommentEntity commentEntity4 = CommentEntityFixture.of(userEntity, recordEntity);
        commentRepository.saveAll(List.of(commentEntity4, commentEntity3, commentEntity2, commentEntity1));

        final int pageNumber = 0;
        final int pageSize = 4;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<CommentEntity> page = commentRepository.findWithCommenterAndRepliesByRecordId(recordEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent())
                .hasSize(4)
                .extracting("id")
                .containsOnly(commentEntity4.getId(), commentEntity3.getId(), commentEntity2.getId(), commentEntity1.getId());
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트가 없으면 빈 배열을 반환한다")
    void findCommentEntityByRecordEntityOrderByCreatedDateTimeAscReturnsEmptyTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 1));

        final int pageNumber = 0;
        final int pageSize = 2;
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        //when
        Page<CommentEntity> page = commentRepository.findWithCommenterAndRepliesByRecordId(recordEntity.getId(), pageRequest);

        //then
        Assertions.assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("기록에 등록된 댓글 리스트를 soft delete한다")
    void deleteAllByRecordEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 1));

        CommentEntity commentEntity1 = CommentEntityFixture.of(userEntity, recordEntity);
        CommentEntity commentEntity2 = CommentEntityFixture.of(userEntity, recordEntity);
        CommentEntity commentEntity3 = CommentEntityFixture.of(userEntity, recordEntity);
        CommentEntity commentEntity4 = CommentEntityFixture.of(userEntity, recordEntity);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when
        commentRepository.deleteAllByRecordEntityId(recordEntity.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("원댓글로 대댓글 리스트를 soft delete한다")
    void deleteAllByCommentEntityTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 1));

        CommentEntity parentComment = CommentEntityFixture.of(userEntity, recordEntity);
        commentRepository.save(parentComment);

        CommentEntity commentEntity1 = CommentEntityFixture.of(userEntity, recordEntity, parentComment);
        CommentEntity commentEntity2 = CommentEntityFixture.of(userEntity, recordEntity, parentComment);
        CommentEntity commentEntity3 = CommentEntityFixture.of(userEntity, recordEntity, parentComment);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3));

        //when
        commentRepository.deleteAllByCommentEntityId(parentComment.getId());

        //then
        Assertions.assertThat(commentRepository.findAll()).containsOnly(parentComment);
    }

    @Test
    @DisplayName("댓글을 soft delete한다")
    void softDeleteTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));
        RecordEntity recordEntity = recordRepository.save(RecordEntityFixture.of(feedEntity.getUserEntity(), feedEntity, 1));
        CommentEntity commentEntity = CommentEntityFixture.of(userEntity, recordEntity, null);
        commentRepository.save(commentEntity);

        //when
        commentRepository.delete(commentEntity);

        //then
        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }
}