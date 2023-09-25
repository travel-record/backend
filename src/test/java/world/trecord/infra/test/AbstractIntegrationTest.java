package world.trecord.infra.test;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import world.trecord.client.feign.client.GoogleTokenFeignClient;
import world.trecord.client.feign.client.GoogleUserInfoFeignClient;
import world.trecord.config.redis.UserCacheRepository;
import world.trecord.controller.feed.FeedValidator;
import world.trecord.controller.record.RecordValidator;
import world.trecord.domain.comment.CommentRepository;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.notification.NotificationRepository;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.record.RecordSequenceRepository;
import world.trecord.domain.userrecordlike.UserRecordLikeRepository;
import world.trecord.domain.users.UserRepository;
import world.trecord.domain.users.UserRepositoryExtension;
import world.trecord.event.notification.NotificationEventListener;
import world.trecord.event.sse.SseEmitterRepository;
import world.trecord.event.sse.SseEmitterService;
import world.trecord.infra.support.IntegrationTestSupport;
import world.trecord.service.comment.CommentService;
import world.trecord.service.feed.FeedService;
import world.trecord.service.feedcontributor.FeedContributorService;
import world.trecord.service.notification.NotificationService;
import world.trecord.service.record.RecordService;
import world.trecord.service.userrecordlike.UserRecordLikeService;
import world.trecord.service.users.UserService;

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

    @MockBean
    protected NotificationEventListener mockEventListener;

    @Autowired
    protected UserService userService;

    @Autowired
    protected RecordValidator recordValidator;

    @Autowired
    protected FeedValidator feedValidator;

    @Autowired
    protected FeedService feedService;

    @Autowired
    protected FeedContributorService feedContributorService;

    @Autowired
    protected SseEmitterService sseEmitterService;

    @Autowired
    protected UserRepositoryExtension userRepositoryExtensionImpl;

    @Autowired
    protected UserCacheRepository userCacheRepository;

    @Autowired
    protected GoogleUserInfoFeignClient googleUserInfoFeignClient;

    @Autowired
    protected GoogleTokenFeignClient googleTokenFeignClient;
}
