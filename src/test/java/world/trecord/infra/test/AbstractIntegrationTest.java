package world.trecord.infra.test;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserRepository;
import world.trecord.event.sse.SseEmitterRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.IntegrationTestSupport;
import world.trecord.service.comment.CommentService;
import world.trecord.service.notification.NotificationService;
import world.trecord.service.record.RecordService;
import world.trecord.service.userrecordlike.UserRecordLikeService;

@IntegrationTestSupport
public abstract class AbstractIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected FeedRepository feedRepository;

    @Autowired
    protected RecordRepository recordRepository;

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected CommentService commentService;

    @Autowired
    protected RecordService recordService;

    @Autowired
    protected UserRecordLikeService userRecordLikeService;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected NotificationService notificationService;

    @Autowired
    protected UserRecordLikeRepository userRecordLikeRepository;

    @Autowired
    protected SseEmitterRepository sseEmitterRepository;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected RecordSequenceRepository recordSequenceRepository;

    @Autowired
    protected FeedContributorRepository feedContributorRepository;
}
