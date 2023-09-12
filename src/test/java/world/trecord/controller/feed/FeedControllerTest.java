package world.trecord.controller.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.security.JwtTokenHandler;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.feedcontributor.FeedContributorRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.AbstractContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.service.feed.request.FeedCreateRequest;
import world.trecord.service.feed.request.FeedUpdateRequest;
import world.trecord.service.feedcontributor.FeedContributorService;
import world.trecord.service.feedcontributor.request.FeedExpelRequest;
import world.trecord.service.feedcontributor.request.FeedInviteRequest;

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
@MockMvcTestSupport
class FeedControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    JwtTokenHandler jwtTokenHandler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RecordRepository recordRepository;

    @Autowired
    JwtProperties jwtProperties;

    @Autowired
    FeedContributorRepository feedContributorRepository;

    @Autowired
    FeedContributorService feedContributorService;

    @Test
    @DisplayName("GET /api/v1/feeds - 성공 (등록된 피드가 없을때)")
    void getEmptyFeedListByUserIdTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header(AUTHORIZATION, createToken(savedUserEntity.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeds").isArray())
                .andExpect(jsonPath("$.data.feeds").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/feeds - 성공")
    void getFeedListByUserIdTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        FeedEntity feedEntity4 = createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now());
        ;
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header(AUTHORIZATION, createToken(savedUserEntity.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeds").isArray())
                .andExpect(jsonPath("$.data.feeds.length()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/feeds - 실패 (인증되지 않는 사용자)")
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
    void getFeedByAuthenticatedUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(savedUserEntity.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (인증되지 않은 사용자)")
    void getFeedByNotAuthenticatedUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/feeds - 성공")
    void createFeedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

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
                                .header(AUTHORIZATION, createToken(savedUserEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/feeds - 실패 (인증되지 않은 사용자)")
    void createFeedByNotAuthenticatedUserTest() throws Exception {
        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 성공")
    void inviteUserTest() throws Exception {
        //given
        UserEntity feedOwner = userRepository.save(createUser("test@email.com"));
        UserEntity invitedUser = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(feedOwner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/invite", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(feedOwner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk());

        Assertions.assertThat(feedContributorRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 실패 (피드 주인이 자기 자신을 초대하는 경우)")
    void inviteSelfTest() throws Exception {
        //given
        UserEntity feedOwner = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(feedOwner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(feedOwner.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/invite", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(feedOwner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(SELF_INVITATION_NOT_ALLOWED.code()));
    }

    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 실패 (이미 초대된 사용자를 초대하는 경우)")
    void inviteAlreadyInvitedUserTest() throws Exception {
        //given
        UserEntity feedOwner = userRepository.save(createUser("test@email.com"));
        UserEntity invitedUser = userRepository.save(createUser("test1@email.com"));
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
                        post("/api/v1/feeds/{feedId}/invite", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(feedOwner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(USER_ALREADY_INVITED));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 실패 (유효하지 않은 토큰인 경우)")
    void inviteUserWithInvalidTokenTest() throws Exception {
        //given
        long feedId = 1L;
        long invalidToken = 0L;

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/invite", feedId)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 실패 (피드가 없는 경우)")
    void inviteUserWhenFeedNotFoundTest() throws Exception {
        //given
        long notExistingFeedId = 0L;
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(0L)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/invite", notExistingFeedId)
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 실패 (피드 관리자의 요청이 아닌 경우)")
    void inviteUserWhenNotFeedOwnerRequestTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(0L)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/invite", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(other.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/invite - 실패 (초대된 사용자가 없는 경우)")
    void inviteUserWhenUserNotFoundTest() throws Exception {
        //given
        long notExistingUserId = 0L;
        UserEntity owner = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.now(), LocalDateTime.now()));

        FeedInviteRequest request = FeedInviteRequest.builder()
                .userToId(notExistingUserId)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/invite", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.code()));
    }


    @Test
    @DisplayName("GET /api/v1/feeds/invited - 성공")
    void getUserParticipatingFeedsTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("owner@email.com"));
        UserEntity invitedUser = userRepository.save(createUser("invitedUser@email.com"));

        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity1 = createFeed(owner, feedTime, feedTime);
        FeedEntity feedEntity2 = createFeed(owner, feedTime, feedTime);
        FeedEntity feedEntity3 = createFeed(owner, feedTime, feedTime);
        FeedEntity feedEntity4 = createFeed(owner, feedTime, feedTime);
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        FeedContributorEntity feedContributor1 = createFeedContributor(invitedUser, feedEntity1);
        FeedContributorEntity feedContributor2 = createFeedContributor(invitedUser, feedEntity2);
        FeedContributorEntity feedContributor3 = createFeedContributor(invitedUser, feedEntity3);
        FeedContributorEntity feedContributor4 = createFeedContributor(invitedUser, feedEntity4);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/invited")
                                .header(AUTHORIZATION, createToken(invitedUser.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/invited - 성공 (초대된 피드가 없는 경우)")
    void getUserParticipatingFeedsWhenFeedsEmptyTest() throws Exception {
        //given
        UserEntity invitedUser = userRepository.save(createUser("invitedUser@email.com"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/invited")
                                .header(AUTHORIZATION, createToken(invitedUser.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/feeds/invited - 실패 (유효하지 않은 토큰으로 요청)")
    void getUserParticipatingFeedsWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/invited")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/feeds/invited - 실패 (토큰없이 요청)")
    void getUserParticipatingFeedsWithoutTokenTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/invited")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }


    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 성공")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
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
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
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
    void updateFeedWhenFeedNotExistingTest() throws Exception {
        //given
        long notExistingFeedId = 0L;
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

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
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (피드 관리자 아님)")
    void updateFeedByFeedManagerTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));
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
                                .header(AUTHORIZATION, createToken(other.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (파라미터 검증 오류)")
    void updateFeedWhenRequestParaemeterErrorTest() throws Exception {
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.now(), LocalDateTime.now()));
        FeedUpdateRequest request = FeedUpdateRequest.builder().build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(userEntity.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId} - 성공 (기록과 함께 삭제)")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.now(), LocalDateTime.now()));
        RecordEntity recordEntity1 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity2 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 3, 0, 0));
        RecordEntity recordEntity3 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 1, 0, 0));

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(savedUserEntity.getId()))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedRepository.findAll()).isEmpty();
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId} - 실패 (피드 관리자 아님)")
    void deleteFeedByManagerTest() throws Exception {
        //given
        UserEntity author = userRepository.save(createUser("test@email.com"));
        UserEntity other = userRepository.save(createUser("test1@email.com"));
        FeedEntity feedEntity = feedRepository.save(createFeed(author, LocalDateTime.now(), LocalDateTime.now()));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(other.getId()))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/feeds/{feedId} - 실패 (존재하지 않는 피드)")
    void deleteNotExistingFeedTest() throws Exception {
        //given
        UserEntity savedUser = userRepository.save(createUser("test@email.com"));

        long notExistingFeedId = 0L;

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", notExistingFeedId)
                                .header(AUTHORIZATION, createToken(savedUser.getId()))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 성공")
    void expelUserTest() throws Exception {
        //given
        UserEntity owner = createUser("test@email.com");
        UserEntity invitedUser = createUser("test1@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        feedContributorRepository.save(createFeedContributor(invitedUser, feedEntity));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedContributorRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (피드 주인이 자신을 내보내려고 하는 경우)")
    void expelUserSelfTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("test@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(owner.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(SELF_EXPELLING_NOT_ALLOWED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (유효하지 않은 토큰으로 요청)")
    void expelUserWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", 1L)
                                .header(AUTHORIZATION, invalidToken)
                                .contentType(APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (인증 토큰 없이 요청)")
    void expelUserWithoutTokenTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("test@email.com"));

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", 1L)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (요청 파라미터 없이 요청)")
    void expelUserWithoutRequestBodyTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("test@email.com"));

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", 1L)
                                .header(AUTHORIZATION, createToken(owner.getId()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (요청 파라미터 올바르지 않을 경우)")
    void expelUserWithInvalidParameterTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("test@email.com"));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (이미 내보내진 사용자를 내보내려고 하는 경우)")
    void expelUserWhoAlreadyExpelled() throws Exception {
        //given
        UserEntity owner = createUser("test@email.com");
        UserEntity invitedUser = createUser("test1@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        feedContributorRepository.save(createFeedContributor(invitedUser, feedEntity));

        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        feedContributorService.expelUserFromFeed(owner.getId(), feedEntity.getId(), request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_NOT_INVITED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (내보려는 사용자가 DB에 없는 경우)")
    void expelUserWhoNotFoundTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(createUser("test@email.com"));
        long notExistingUserId = 0L;
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(notExistingUserId)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (피드 주인의 요청이 아닌 경우)")
    void expelUserByNotOwnerTest() throws Exception {
        UserEntity owner = createUser("test@email.com");
        UserEntity other = createUser("test1@email.com");
        UserEntity invitedUser = createUser("test2@email.com");
        userRepository.saveAll(List.of(owner, other, invitedUser));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(other.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (초대되지 않은 사용자를 내보내려는 경우)")
    void expelUserWhoNotInvitedTest() throws Exception {
        //given
        UserEntity owner = createUser("test@email.com");
        UserEntity invitedUser = createUser("test2@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));
        LocalDateTime feedTime = LocalDateTime.of(2022, 3, 1, 0, 0);
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, feedTime, feedTime));
        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", feedEntity.getId())
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(USER_NOT_INVITED.code()));
    }

    @Test
    @DisplayName("POST /api/v1/feeds/{feedId}/expel - 실패 (피드가 존재하지 않는 경우)")
    void expelUserWhenFeedNotFoundTest() throws Exception {
        //given
        UserEntity owner = createUser("test@email.com");
        UserEntity invitedUser = createUser("test2@email.com");
        userRepository.saveAll(List.of(owner, invitedUser));

        long notExisingFeedId = 0L;
        FeedExpelRequest request = FeedExpelRequest.builder()
                .userToId(invitedUser.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds/{feedId}/expel", notExisingFeedId)
                                .header(AUTHORIZATION, createToken(owner.getId()))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(FEED_NOT_FOUND.code()));
    }

    private String createToken(Long userId) {
        return jwtTokenHandler.generateToken(userId, jwtProperties.getSecretKey(), jwtProperties.getTokenExpiredTimeMs());
    }

    private UserEntity createUser(String mail) {
        return UserEntity.builder()
                .email(mail)
                .build();
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
                .date(date)
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .build();
    }

    private FeedContributorEntity createFeedContributor(UserEntity userEntity, FeedEntity feedEntity) {
        return FeedContributorEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .build();
    }
}