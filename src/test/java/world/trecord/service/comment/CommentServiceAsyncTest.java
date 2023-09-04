package world.trecord.service.comment;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.comment.request.CommentCreateRequest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@IntegrationTestSupport
public class CommentServiceAsyncTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    CommentService commentService;

    @Autowired
    NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        recordRepository.deleteAll();
        feedRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("다른 사람의 기록에 댓글을 작성하면 비동기로 알림이 생성된다")
    void createCommentNotificationWhenCommentOnOtherRecordTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser());
        UserEntity commenter = userRepository.save(createUser());
        FeedEntity feedEntity = feedRepository.save(createFeed(author));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .recordId(recordEntity.getId())
                .content("content")
                .build();

        //when
        log.info("createComment started in thread: {}", Thread.currentThread().getName());
        final CountDownLatch latch = new CountDownLatch(1);

        commentService.createComment(commenter.getId(), request);

        latch.await(1, TimeUnit.SECONDS);
        log.info("createComment ended in thread: {}", Thread.currentThread().getName());

        //then
        Assertions.assertThat(notificationRepository.findAll()).hasSize(1);
    }

    private UserEntity createUser() {
        return UserEntity.builder()
                .email(UUID.randomUUID().toString() + System.currentTimeMillis() + Thread.currentThread().getId())
                .build();
    }

    private FeedEntity createFeed(UserEntity userEntity) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("feed name")
                .startAt(LocalDateTime.of(2022, 9, 30, 0, 0))
                .endAt(LocalDateTime.of(2022, 10, 2, 0, 0))
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .date(LocalDateTime.of(2022, 10, 10, 0, 0))
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }
}
