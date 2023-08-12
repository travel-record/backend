package world.trecord.web.controller.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import world.trecord.MockMvcTestSupport;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feed.FeedRepository;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.record.RecordRepository;
import world.trecord.domain.users.UserEntity;
import world.trecord.domain.users.UserRepository;
import world.trecord.web.security.JwtProvider;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedDeleteRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.INVALID_TOKEN;

@MockMvcTestSupport
class FeedControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RecordRepository recordRepository;

    @Test
    @DisplayName("사용자가 등록한 피드가 없다면 feeds 필드에 빈 배열을 반환한다")
    void getEmptyFeedListByUserIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();

        UserEntity savedUserEntity = userRepository.save(userEntity);

        String token = jwtProvider.createTokenWith(savedUserEntity.getId());

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeds").isArray())
                .andExpect(jsonPath("$.data.feeds").isEmpty());
    }

    @Test
    @DisplayName("사용자가 등록한 feed가 있다면 여행 시작 시간 내림차순으로 정렬하여 feed 배열을 반환한다")
    void getFeedListByUserIdTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();

        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity1 = createFeedEntity(savedUserEntity, "feed name1", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity feedEntity2 = createFeedEntity(savedUserEntity, "feed name2", LocalDateTime.of(2021, 10, 4, 0, 0), LocalDateTime.of(2021, 10, 15, 0, 0));
        FeedEntity feedEntity3 = createFeedEntity(savedUserEntity, "feed name3", LocalDateTime.of(2021, 12, 10, 0, 0), LocalDateTime.of(2021, 12, 20, 0, 0));
        FeedEntity feedEntity4 = createFeedEntity(savedUserEntity, "feed name4", LocalDateTime.of(2021, 12, 21, 0, 0), LocalDateTime.of(2021, 12, 25, 0, 0));

        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        String token = jwtProvider.createTokenWith(savedUserEntity.getId());

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header("Authorization", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.feeds").isArray())
                .andExpect(jsonPath("$.data.feeds.length()").value(4))
                .andExpect(jsonPath("$.data.feeds[0].name").value("feed name4"))
                .andExpect(jsonPath("$.data.feeds[0].startAt").value("2021-12-21"))
                .andExpect(jsonPath("$.data.feeds[0].endAt").value("2021-12-25"))
                .andExpect(jsonPath("$.data.feeds[1].name").value("feed name3"))
                .andExpect(jsonPath("$.data.feeds[2].name").value("feed name2"))
                .andExpect(jsonPath("$.data.feeds[3].name").value("feed name1"));
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 601 에러 응답 코드를 반환한다")
    void getFeedListNotExistingUserTest() throws Exception {
        //given
        String token = jwtProvider.createTokenWith(0L);

        //when //then
        mockMvc.perform(
                        get("/api/v1/feeds")
                                .header("Authorization", token)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.getErrorCode()))
                .andExpect(jsonPath("$.message").value(INVALID_TOKEN.getErrorMsg()));
    }


    private FeedEntity createFeedEntity(UserEntity saveUserEntity, String name, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(saveUserEntity)
                .name(name)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    @Test
    @DisplayName("사용자가 피드를 생성하면 생성된 피드 정보를 반환한다")
    void createFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();

        UserEntity savedUserEntity = userRepository.save(userEntity);

        String token = jwtProvider.createTokenWith(savedUserEntity.getId());

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

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        post("/api/v1/feeds")
                                .header("Authorization", token)
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자가 작성한 피드를 수정하면 수정된 피드 정보를 반환한다")
    void updateFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        FeedEntity feedEntity = createFeedEntity(savedUserEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        String token = jwtProvider.createTokenWith(savedUserEntity.getId());

        String updateFeedName = "updated feed name";
        String updatedFeedImage = "updated feed image url";
        String updatedFeedDescription = "updated feed description";
        LocalDateTime updatedStartAt = LocalDateTime.of(2022, 9, 1, 0, 0);
        LocalDateTime updatedEndAt = LocalDateTime.of(2022, 9, 30, 0, 0);

        FeedUpdateRequest request = FeedUpdateRequest.builder()
                .id(savedFeedEntity.getId())
                .name(updateFeedName)
                .imageUrl(updatedFeedImage)
                .description(updatedFeedDescription)
                .startAt(updatedStartAt)
                .endAt(updatedEndAt)
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        put("/api/v1/feeds", savedFeedEntity.getId())
                                .header("Authorization", token)
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        FeedEntity updatedFeedEntity = feedRepository.findById(savedFeedEntity.getId()).get();

        Assertions.assertThat(updatedFeedEntity.getName()).isEqualTo(updateFeedName);
        Assertions.assertThat(updatedFeedEntity.getImageUrl()).isEqualTo(updatedFeedImage);
        Assertions.assertThat(updatedFeedEntity.getDescription()).isEqualTo(updatedFeedDescription);
        Assertions.assertThat(updatedFeedEntity.getStartAt()).isEqualTo(updatedStartAt);
        Assertions.assertThat(updatedFeedEntity.getEndAt()).isEqualTo(updatedEndAt);
    }

    @Test
    @DisplayName("사용자가 피드를 삭제하면 삭제된 피드 아이디를 반환한다")
    void deleteFeedTest() throws Exception {
        //given
        UserEntity userEntity = UserEntity.builder()
                .email("test@email.com")
                .build();
        UserEntity savedUserEntity = userRepository.save(userEntity);

        String token = jwtProvider.createTokenWith(savedUserEntity.getId());

        FeedEntity feedEntity = createFeedEntity(savedUserEntity, "feed name", LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0));
        FeedEntity savedFeedEntity = feedRepository.save(feedEntity);

        RecordEntity recordEntity1 = createRecordEntity(feedEntity, "record1", "place2", LocalDateTime.of(2022, 3, 2, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity2 = createRecordEntity(feedEntity, "record2", "place3", LocalDateTime.of(2022, 3, 3, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        RecordEntity recordEntity3 = createRecordEntity(feedEntity, "record3", "place1", LocalDateTime.of(2022, 3, 1, 0, 0), "content1", "weather1", "satisfaction1", "feeling1");
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3));

        FeedDeleteRequest request = FeedDeleteRequest.builder()
                .id(savedFeedEntity.getId())
                .build();

        String content = objectMapper.writeValueAsString(request);

        //when //then
        mockMvc.perform(
                        delete("/api/v1/feeds", savedFeedEntity.getId())
                                .header("Authorization", token)
                                .content(content)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        Assertions.assertThat(feedRepository.findById(savedFeedEntity.getId())).isEmpty();
        Assertions.assertThat(recordRepository.findAll()).isEmpty();
    }

    private RecordEntity createRecordEntity(FeedEntity feedEntity, String record, String place, LocalDateTime date, String content, String weather, String satisfaction, String feeling) {
        return RecordEntity.builder()
                .feedEntity(feedEntity)
                .title(record)
                .place(place)
                .date(date)
                .content(content)
                .weather(weather)
                .transportation(satisfaction)
                .feeling(feeling)
                .build();
    }
}