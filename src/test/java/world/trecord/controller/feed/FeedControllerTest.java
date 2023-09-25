package world.trecord.controller.feed;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorStatus;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.feed.request.FeedCreateRequest;
import world.trecord.dto.feed.request.FeedUpdateRequest;
import world.trecord.dto.feedcontributor.request.FeedInviteRequest;
import world.trecord.infra.fixture.FeedContributorFixture;
import world.trecord.infra.fixture.FeedEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.support.WithTestUser;
import world.trecord.infra.test.AbstractMockMvcTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
class FeedControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("GET /api/v1/feeds - 성공 (등록된 피드가 없을때)")
    @WithTestUser
    void getEmptyFeedListByUserIdTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/feeds - 성공")
    @WithTestUser("user@email.com")
    void getFeedListByUserIdTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.findByEmail("user@email.com").get();

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        FeedEntity feedEntity4 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        ;
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/feeds - 실패 (인증되지 않는 사용자)")
    @WithAnonymousUser
    void getFeedListNotExistingUserTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }


    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (인증된 사용자)")
    @WithTestUser("user@email.com")
    void getFeedByAuthenticatedUserTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        UserEntity contributor = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.now(), LocalDateTime.now()));

        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        feedContributorRepository.findTopByUserIdAndFeedIdOrderByModifiedAtDesc(contributor.getId(), feedEntity.getId());
        entityManager.flush();
        entityManager.clear();

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (피드 주인은 피드를 수정할 수 있고 피드 아래에 기록을 작성할 수 있다)")
    @WithTestUser("user@email.com")
    void getFeed_byFeedOwner_returnCanModifyFeedTrue() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canWriteRecord").value(true))
                .andExpect(jsonPath("$.data.canModifyFeed").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (피드 컨트리뷰터는 피드 아래에 기록을 작성할 수 있고 피드 수정은 불가능하다)")
    @WithTestUser("contributor@email.com")
    void getFeed_byFeedContributor_returnCanWriteRecordTrue() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity contributor = userRepository.findByEmail("contributor@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header(AUTHORIZATION, token(contributor.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canWriteRecord").value(true))
                .andExpect(jsonPath("$.data.canModifyFeed").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (피드 주인이나 피드 컨트리뷰터가 아니면 피드 수정과 피드 아래에 기록을 작성할 수 없다)")
    @WithAnonymousUser
    void getFeed_byNotFeedContributorAndOwner_returnCanWriteRecordFalse() throws Exception {
        //given
        UserEntity owner = UserEntityFixture.of("owner@email.com");
        UserEntity contributor = UserEntityFixture.of("contributor@email.com");
        userRepository.saveAll(List.of(owner, contributor));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canWriteRecord").value(false))
                .andExpect(jsonPath("$.data.canModifyFeed").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (피드 컨트리뷰터 리스트 조회)")
    @WithTestUser
    void getFeed_byFeedOwner() throws Exception {
        //given
        UserEntity userEntity = UserEntityFixture.of();
        UserEntity contributor1 = UserEntityFixture.of();
        UserEntity contributor2 = UserEntityFixture.of();
        UserEntity contributor3 = UserEntityFixture.of();
        UserEntity contributor4 = UserEntityFixture.of();
        UserEntity contributor5 = UserEntityFixture.of();
        userRepository.saveAll(List.of(userEntity, contributor1, contributor2, contributor3, contributor4, contributor5));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.now(), LocalDateTime.now()));
        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(contributor1, feedEntity);
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(contributor2, feedEntity);
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(contributor3, feedEntity);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(contributor4, feedEntity);
        FeedContributorEntity feedContributor5 = FeedContributorFixture.of(contributor5, feedEntity);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4, feedContributor5));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contributors.size()").value(6));
    }

    @Test
    @DisplayName("가장 최근에 피드에서 쫓겨나간 사용자는 피드를 조회할 수 없다")
    @WithTestUser("user@email.com")
    void getFeed_byUserExpelledRecently_returnsForbiddenCode() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity contributor = userRepository.findByEmail("user@email.com").get();

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(contributor.getId(), feedEntity.getId(), FeedContributorStatus.EXPELLED);
        entityManager.flush();
        entityManager.clear();

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @CsvSource({"LEFT", "EXPELLED"})
    @ParameterizedTest
    @DisplayName("피드에 나갔다가 다시 피드에 참여하는 사용자는 피드를 조회할 수 있다")
    @WithTestUser("user@email.com")
    void getFeed_byUserExpelledOrLeftBefore_returnsForbiddenCode(FeedContributorStatus status) throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity contributor = userRepository.findByEmail("user@email.com").get();

        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(owner));
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        feedContributorRepository.updateStatusAndDeleteByUserEntityIdAndFeedEntityId(contributor.getId(), feedEntity.getId(), status);
        feedContributorRepository.save(FeedContributorFixture.of(contributor, feedEntity));
        entityManager.flush();
        entityManager.clear();

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (미인증 사용자)")
    @WithAnonymousUser
    void getFeedByNotAuthenticatedUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/feeds - 성공")
    @WithTestUser
    void createFeedTest() throws Exception {
        //given
        String feedName = "feed name";
        String imageUrl = "image";
        String companion = "companion1 companion2";
        LocalDateTime startAt = LocalDateTime.of(2022, 12, 25, 0, 0);
        LocalDateTime endAt = LocalDateTime.of(2022, 12, 30, 0, 0);
        String place = "jeju";
        String satisfaction = "good";
        String description = "description";

        FeedCreateRequest request = FeedCreateRequest.builder()
                .name(feedName)
                .companion(companion)
                .imageUrl(imageUrl)
                .description(description)
                .startAt(startAt)
                .endAt(endAt)
                .place(place)
                .satisfaction(satisfaction)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/feeds - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void createFeedByNotAuthenticatedUserTest() throws Exception {
        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId}/records - 성공 (인증된 사용자)")
    @WithTestUser("test1@gmail.com")
    void getFeedRecordsTest() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of("test@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(user, feedTime, feedTime));
        LocalDateTime recordTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        RecordEntity recordEntity1 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity2 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity3 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity4 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity5 = createRecord(feedEntity, recordTime);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4, recordEntity5));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}/records", feedEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId}/records - 성공 (미인증 사용자)")
    @WithAnonymousUser
    void getFeedRecordsWithoutTokenTest() throws Exception {
        //given
        UserEntity user = userRepository.save(UserEntityFixture.of("test@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(user, feedTime, feedTime));
        LocalDateTime recordTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        RecordEntity recordEntity1 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity2 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity3 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity4 = createRecord(feedEntity, recordTime);
        RecordEntity recordEntity5 = createRecord(feedEntity, recordTime);

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4, recordEntity5));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}/records", feedEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId}/records - 실패 (존재하지 않는 피드 아이디로 요청)")
    void getFeedRecordsWithNotExistingFeedIdTest() throws Exception {
        //given
        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}/records", notExistingFeedId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId}/records - 실패 (유효하지 않은 토큰으로 요청)")
    void getFeedRecordsWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";
        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}/records", notExistingFeedId)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 성공")
    @WithTestUser("owner@email.com")
    void inviteUserTest() throws Exception {
        //given
        UserEntity feedOwner = userRepository.findByEmail("owner@email.com").get();
        UserEntity invitedUser = userRepository.save(UserEntityFixture.of("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(feedOwner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", feedEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isOk());

        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 실패 (피드 주인이 자기 자신을 초대하는 경우)")
    @WithTestUser("owner@email.com")
    void inviteSelfTest() throws Exception {
        //given
        UserEntity feedOwner = userRepository.findByEmail("owner@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(feedOwner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(feedOwner.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", feedEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(SELF_INVITATION_NOT_ALLOWED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 실패 (이미 초대된 사용자를 초대하는 경우)")
    @WithTestUser("owner@email.com")
    void inviteAlreadyInvitedUserTest() throws Exception {
        //given
        UserEntity feedOwner = userRepository.findByEmail("owner@email.com").get();
        UserEntity invitedUser = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(createFeed(feedOwner, LocalDateTime.now(), LocalDateTime.now()));
        feedContributorRepository.save(FeedContributorEntity.builder()
                .userEntity(invitedUser)
                .feedEntity(feedEntity)
                .build());

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", feedEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(USER_ALREADY_INVITED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 실패 (유효하지 않은 토큰인 경우)")
    void inviteUserWithInvalidTokenTest() throws Exception {
        //given
        long feedId = 1L;
        long invalidToken = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", feedId)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 실패 (피드가 없는 경우)")
    @WithTestUser
    void inviteUserWhenFeedNotFoundTest() throws Exception {
        //given
        long notExistingFeedId = 0L;

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(0L)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", notExistingFeedId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 실패 (피드 관리자의 요청이 아닌 경우)")
    @WithTestUser("other@email.com")
    void inviteUserWhenNotFeedOwnerRequestTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other = userRepository.findByEmail("other@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(0L)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", feedEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/invite - 실패 (초대된 사용자가 없는 경우)")
    @WithTestUser("owner@email.com")
    void inviteUserWhenUserNotFoundTest() throws Exception {
        //given
        long notExistingUserId = 0L;
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(notExistingUserId)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/contributors/invite", feedEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 성공")
    @WithTestUser("user@email.com")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        FeedEntity savedFeed = feedRepository.save(createFeed(userEntity, LocalDateTime.now(), LocalDateTime.now()));

        String updateFeedName = "updated feed name";
        String updatedFeedImage = "updated feed image url";
        String updatedFeedDescription = "updated feed description";
        LocalDateTime updatedStartAt = LocalDateTime.of(2022, 9, 1, 0, 0);
        LocalDateTime updatedEndAt = LocalDateTime.of(2022, 9, 30, 0, 0);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .name(updateFeedName)
                .imageUrl(updatedFeedImage)
                .description(updatedFeedDescription)
                .startAt(updatedStartAt)
                .endAt(updatedEndAt)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds/{feedId}", savedFeed.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedRepository.findById(savedFeed.getId()))
                .isPresent()
                .hasValueSatisfying(
                        feedEntity -> {
                            Assertions.assertThat(feedEntity.getName()).isEqualTo(updateFeedName);
                            Assertions.assertThat(feedEntity.getImageUrl()).isEqualTo(updatedFeedImage);
                            Assertions.assertThat(feedEntity.getDescription()).isEqualTo(updatedFeedDescription);
                            Assertions.assertThat(feedEntity.getStartAt()).isEqualTo(updatedStartAt);
                            Assertions.assertThat(feedEntity.getEndAt()).isEqualTo(updatedEndAt);
                        }
                );
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (존재하지 않는 피드)")
    @WithTestUser("user@email.com")
    void updateFeedWhenFeedNotExistingTest() throws Exception {
        //given
        long notExistingFeedId = 0L;

        String updateFeedName = "updated feed name";
        String updatedFeedImage = "updated feed image url";
        String updatedFeedDescription = "updated feed description";
        LocalDateTime updatedStartAt = LocalDateTime.of(2022, 9, 1, 0, 0);
        LocalDateTime updatedEndAt = LocalDateTime.of(2022, 9, 30, 0, 0);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .name(updateFeedName)
                .imageUrl(updatedFeedImage)
                .description(updatedFeedDescription)
                .startAt(updatedStartAt)
                .endAt(updatedEndAt)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds/{feedId}", notExistingFeedId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (피드 관리자 아님)")
    @WithTestUser("other@email.com")
    void updateFeedByFeedManagerTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other = userRepository.findByEmail("other@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(author, LocalDateTime.now(), LocalDateTime.now()));

        String updateFeedName = "updated feed name";
        String updatedFeedImage = "updated feed image url";
        String updatedFeedDescription = "updated feed description";
        LocalDateTime updatedStartAt = LocalDateTime.of(2022, 9, 1, 0, 0);
        LocalDateTime updatedEndAt = LocalDateTime.of(2022, 9, 30, 0, 0);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .name(updateFeedName)
                .imageUrl(updatedFeedImage)
                .description(updatedFeedDescription)
                .startAt(updatedStartAt)
                .endAt(updatedEndAt)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (파라미터 검증 오류)")
    @WithTestUser
    void updateFeedWhenRequestParaemeterErrorTest() throws Exception {
        //given
        long notExistingUserId = 0L;
        FeedUpdateRequest request = FeedUpdateRequest.builder().build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds/{feedId}", notExistingUserId)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId} - 성공 (기록과 함께 삭제)")
    @WithTestUser
    void deleteFeedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.findByEmail("test@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now()));
        RecordEntity recordEntity1 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity2 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 3, 0, 0));
        RecordEntity recordEntity3 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 1, 0, 0));

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedRepository.findAll()).isEmpty();
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId} - 실패 (피드 관리자 아님)")
    @WithTestUser("test1@email.com")
    void deleteFeedByManagerTest() throws Exception {
        //given
        UserEntity author = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other = userRepository.findByEmail("test1@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(author, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId} - 실패 (존재하지 않는 피드)")
    @WithTestUser
    void deleteNotExistingFeedTest() throws Exception {
        //given
        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", notExistingFeedId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 성공")
    @WithTestUser("owner@email.com")
    void expelUserTest() throws Exception {
        //given
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        UserEntity invitedUser = UserEntityFixture.of("test1@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", feedEntity.getId(), invitedUser.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (피드 주인이 자신을 내보내려고 하는 경우)")
    @WithTestUser("owner@email.com")
    void expelUserSelfTest() throws Exception {
        //given
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", feedEntity.getId(), owner.getId())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(SELF_EXPELLING_NOT_ALLOWED.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (유효하지 않은 토큰으로 요청)")
    void expelUserWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", 1L, 1L)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (인증 토큰 없이 요청)")
    @WithAnonymousUser
    void expelUserWithoutTokenTest() throws Exception {
        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", 1L, 1L)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (잘못된 Path variable으로 요청)")
    @WithTestUser
    void expelUserWithInvalidPathVariableTest() throws Exception {
        //given
        String invalidPath = "invalid";

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", 1L, invalidPath)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (이미 내보내진 사용자를 내보내려고 하는 경우)")
    @WithTestUser("owner@email.com")
    void expelUserWhoAlreadyExpelled() throws Exception {
        //given
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        UserEntity invitedUser = userRepository.save(UserEntityFixture.of("test1@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        feedContributorService.expelUserFromFeed(owner.getId(), invitedUser.getId(), feedEntity.getId());

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", feedEntity.getId(), invitedUser.getId())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_NOT_INVITED.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (내보려는 사용자가 DB에 없는 경우)")
    @WithTestUser("owner@email.com")
    void expelUserWhoNotFoundTest() throws Exception {
        //given
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        long notExistingUserId = 0L;
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", feedEntity.getId(), notExistingUserId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (피드 주인의 요청이 아닌 경우)")
    @WithTestUser("other@email.com")
    void expelUserByNotOwnerTest() throws Exception {
        UserEntity owner = UserEntityFixture.of("test@email.com");
        UserEntity other = userRepository.findByEmail("other@email.com").get();
        UserEntity invitedUser = UserEntityFixture.of("test2@email.com");
        userRepository.saveAll(List.of(owner, other, invitedUser));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", feedEntity.getId(), other.getId())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (초대되지 않은 사용자를 내보내려는 경우)")
    @WithTestUser("owner@email.com")
    void expelUserWhoNotInvitedTest() throws Exception {
        //given
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        UserEntity user = userRepository.save(UserEntityFixture.of("test2@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", feedEntity.getId(), user.getId())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_NOT_INVITED.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId}/contributors/{contributorId} - 실패 (피드가 존재하지 않는 경우)")
    @WithTestUser
    void expelUserWhenFeedNotFoundTest() throws Exception {
        //given
        long notExisingFeedId = 0L;
        long notExisingUserId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/{contributorId}", notExisingFeedId, notExisingUserId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/leave - 성공")
    @WithTestUser("invited@email.com")
    void leaveFeedTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity invitedUser = userRepository.findByEmail("invited@email.com").get();
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        feedContributorRepository.save(FeedContributorFixture.of(invitedUser, feedEntity));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/leave", feedEntity.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/leave - 실패 (피드 주인인 경우)")
    @WithTestUser("owner@email.com")
    void leaveFeedByFeedOwnerTest() throws Exception {
        //given
        UserEntity owner = userRepository.findByEmail("owner@email.com").get();
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/leave", feedEntity.getId())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(FEED_OWNER_LEAVING_NOT_ALLOWED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/leave - 실패 (피드 컨트리뷰터가 아닌 경우)")
    @WithTestUser("other@email.com")
    void leaveFeedByWhoNotFeedContributorTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity other = userRepository.findByEmail("other@email.com").get();
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/leave", feedEntity.getId())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_NOT_INVITED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/leave - 실패 (피드가 존재하지 않는 경우)")
    @WithTestUser("owner@email.com")
    void leaveFeedThatNotExistsTest() throws Exception {
        //given
        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/leave", notExistingFeedId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/leave - 실패 (인증 토큰 없이 요청한 경우)")
    @WithAnonymousUser
    void leaveFeedWithoutTokenTest() throws Exception {
        //given
        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/leave", notExistingFeedId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/contributors/leave - 실패 (유효하지 않은 토큰으로 요청한 경우)")
    void leaveFeedWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";
        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}/contributors/leave", notExistingFeedId)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private RecordEntity createRecord(FeedEntity feedEntity, LocalDateTime date) {
        return RecordEntity.builder()
                .userEntity(feedEntity.getUserEntity())
                .feedEntity(feedEntity)
                .title("title")
                .place("place")
                .longitude("longitude")
                .latitude("latitude")
                .date(date)
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }
}