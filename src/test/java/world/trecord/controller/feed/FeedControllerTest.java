package world.trecord.controller.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.config.properties.JwtProperties;
import world.trecord.config.security.JwtTokenHandler;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.infra.ContainerBaseTest;
import world.trecord.infra.MockMvcTestSupport;
import world.trecord.service.feed.request.FeedCreateRequest;
import world.trecord.service.feed.request.FeedUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
@MockMvcTestSupport
class FeedControllerTest extends ContainerBaseTest {

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

    @Test
    @DisplayName("GET /api/v1/feeds - 성공 (등록된 피드가 없을때)")
    void getEmptyFeedListByUserIdTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header("Authorization", createToken(savedUserEntity.getId()))
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

        FeedEntity feedEntity1 = createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeed(savedUserEntity, LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeed(savedUserEntity, LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        FeedEntity feedEntity4 = createFeed(savedUserEntity, LocalDateTime.of(2021, 12, 21, 0, 0), LocalDateTime.of(2021, 12, 25, 0, 0));
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header("Authorization", createToken(savedUserEntity.getId()))
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

        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header("Authorization", createToken(savedUserEntity.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/feeds/{feedId} - 성공 (인증되지 않은 사용자)")
    void getFeedByNotAuthenticatedUserTest() throws Exception {
        //given
        UserEntity savedUserEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

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
                                .header("Authorization", createToken(savedUserEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
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
    @DisplayName("PUT /api/v1/feeds/{feedId} - 성공")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

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
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        FeedEntity updatedFeedEntity = feedRepository.findById(feedEntity.getId()).get();

        Assertions.assertThat(updatedFeedEntity)
                .extracting("name", "imageUrl", "description", "startAt", "endAt")
                .containsExactly(updateFeedName, updatedFeedImage, updatedFeedDescription, updatedStartAt, updatedEndAt);
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (존재하지 않는 피드)")
    void updateFeedWhenFeedNotExistingTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

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
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
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

        FeedEntity feedEntity = feedRepository.save(createFeed(author, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

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
                                .header("Authorization", createToken(other.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/feeds/{feedId} - 실패 (파라미터 검증 오류)")
    void updateFeedWhenRequestParaemeterErrorTest() throws Exception {
        UserEntity userEntity = userRepository.save(createUser("test@email.com"));

        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        FeedUpdateRequest request = FeedUpdateRequest.builder().build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header("Authorization", createToken(userEntity.getId()))
                                .contentType(MediaType.APPLICATION_JSON)
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

        FeedEntity feedEntity = feedRepository.save(createFeed(savedUserEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0));
        RecordEntity recordEntity2 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 3, 0, 0));
        RecordEntity recordEntity3 = createRecord(feedEntity, LocalDateTime.of(2022, 3, 1, 0, 0));

        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header("Authorization", createToken(savedUserEntity.getId()))
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

        FeedEntity feedEntity = feedRepository.save(createFeed(author, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds/{feedId}", feedEntity.getId())
                                .header("Authorization", createToken(other.getId()))
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
                                .header("Authorization", createToken(savedUser.getId()))
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
}