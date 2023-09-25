package world.trecord.controller.record;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordSequenceSwapRequest;
import world.trecord.dto.record.request.RecordUpdateRequest;
import world.trecord.infra.fixture.CommentEntityFixture;
import world.trecord.infra.fixture.UserEntityFixture;
import world.trecord.infra.fixture.UserRecordLikeFixture;
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
class RecordControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 성공 (피드 주인은 기록을 수정할 수 있다)")
    @WithTestUser("user@email.com")
    void getRecordInfoByWriterTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.canModifyRecord").value(true))
                .andExpect(jsonPath("$.data.author.userId").value(writer.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 성공(피드 컨트리뷰터는 자신이 작성한 기록을 수정할 수 있다)")
    @WithTestUser("user@email.com")
    void getRecord_byFeedContributor_returnCanModifyRecordTrue() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of());
        UserEntity contributor = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(owner, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(contributor, feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(owner.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.canModifyRecord").value(true))
                .andExpect(jsonPath("$.data.author.userId").value(contributor.getId()));
    }

    // TODO 피드 컨트리뷰터는 자신이 작성한 기록을 수정할 수 있다
    // TODO 피드 컨트리뷰터는 자신이 작성한 기록을 삭제할 수 있다

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 성공 (인증되지 않은 사용자)")
    @WithAnonymousUser
    void getRecordInfoByWhoNotAuthenticatedTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntityFixture.of());
        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.writerId").value(writer.getId()))
                .andExpect(jsonPath("$.data.title").value(recordEntity.getTitle()))
                .andExpect(jsonPath("$.data.content").value(recordEntity.getContent()))
                .andExpect(jsonPath("$.data.canModifyRecord").value(false))
                .andExpect(jsonPath("$.data.author.userId").value(writer.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 실패 (인증 토큰 검증 실패)")
    void getRecordInfoWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when // then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", 0L)
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId} - 실패 (존재하지 않는 기록 아이디로 조회)")
    @WithTestUser
    void getRecordInfoByNotExistingRecordIdTest() throws Exception {
        //given
        long notExistingRecordId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}", notExistingRecordId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(RECORD_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records - 실패 (올바르지 않은 요청 파라미터)")
    @WithTestUser
    void createRecordWithInvalidParameterTest() throws Exception {
        //given
        RecordCreateRequest request = RecordCreateRequest.builder()
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records - 성공")
    @WithTestUser("user@email.com")
    void createRecordWithValidParameterTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        String title = "title";
        String place = "jeju";
        String feeling = "feeling";
        String weather = "weather";
        String satisfaction = "best";
        String content = "content";
        String companion = "companion";
        String imageUrl = "https://www.image.com";
        String longitude = "longitude";
        String latitude = "latitude";
        LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 1, 0, 0);

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title(title)
                .date(localDateTime)
                .place(place)
                .feeling(feeling)
                .weather(weather)
                .latitude(latitude)
                .longitude(longitude)
                .transportation(satisfaction)
                .content(content)
                .companion(companion)
                .imageUrl(imageUrl)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(recordRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /api/v1/records - 실패 (피드 관리자가 아닌 사용자가 요청)")
    @WithTestUser("user@email.com")
    void createRecordTestWhenUserIsNotManager() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntityFixture.of());
        UserEntity viewer = userRepository.findByEmail("user@email.com").get();

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        String title = "title";
        String place = "jeju";
        String feeling = "feeling";
        String weather = "weather";
        String satisfaction = "best";
        String content = "content";
        String companion = "companion";
        String imageUrl = "https://www.image.com";
        String longitude = "longitude";
        String latitude = "latitude";
        LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 1, 0, 0);

        RecordCreateRequest request = RecordCreateRequest.builder()
                .feedId(feedEntity.getId())
                .title(title)
                .date(localDateTime)
                .place(place)
                .longitude(longitude)
                .latitude(latitude)
                .feeling(feeling)
                .weather(weather)
                .transportation(satisfaction)
                .content(content)
                .companion(companion)
                .imageUrl(imageUrl)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/records/{recordId} - 성공")
    @WithTestUser("user@email.com")
    void updateRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity savedRecord = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));

        String changedTitle = "change title";
        LocalDateTime changedDate = LocalDateTime.of(2021, 10, 2, 0, 0);
        String changedPlace = "changed place";
        String changedLongitude = "changed longitude";
        String changedLatitude = "changed latitude";
        String changedContent = "changed content";
        String changedFeeling = "changed feeling";
        String changedWeather = "changed weather";
        String changedCompanion = "changed changedCompanion";
        String changedTransportation = "changed transportation";
        String changedImageUrl = "changed image url";

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .title(changedTitle)
                .date(changedDate)
                .place(changedPlace)
                .latitude(changedLatitude)
                .longitude(changedLongitude)
                .content(changedContent)
                .feeling(changedFeeling)
                .weather(changedWeather)
                .companion(changedCompanion)
                .transportation(changedTransportation)
                .imageUrl(changedImageUrl)
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", savedRecord.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(recordRepository.findById(savedRecord.getId()))
                .isPresent()
                .hasValueSatisfying(
                        recordEntity -> {
                            Assertions.assertThat(recordEntity.getTitle()).isEqualTo(changedTitle);
                            Assertions.assertThat(recordEntity.getDate()).isEqualTo(changedDate);
                            Assertions.assertThat(recordEntity.getPlace()).isEqualTo(changedPlace);
                            Assertions.assertThat(recordEntity.getContent()).isEqualTo(changedContent);
                            Assertions.assertThat(recordEntity.getFeeling()).isEqualTo(changedFeeling);
                            Assertions.assertThat(recordEntity.getWeather()).isEqualTo(changedWeather);
                            Assertions.assertThat(recordEntity.getCompanion()).isEqualTo(changedCompanion);
                            Assertions.assertThat(recordEntity.getTransportation()).isEqualTo(changedTransportation);
                            Assertions.assertThat(recordEntity.getImageUrl()).isEqualTo(changedImageUrl);
                        }
                );
    }

    @Test
    @DisplayName("PUT /api/v1/records/{recordId} - 실패 (피드 관리자가 아닌 사용자가 요청)")
    @WithTestUser("other@email.com")
    void updateRecordByNotManagerTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntityFixture.of());
        UserEntity other = userRepository.findByEmail("other@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));

        RecordUpdateRequest request = RecordUpdateRequest.builder()
                .title("change title")
                .date(LocalDateTime.of(2021, 10, 2, 0, 0))
                .place("changed place")
                .content("changed content")
                .longitude("changed longitude")
                .latitude("changed latitude")
                .feeling("changed feeling")
                .weather("changed weather")
                .companion("changed changedCompanion")
                .transportation("changed satisfaction")
                .imageUrl("changed image url")
                .build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", recordEntity.getId())
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records/sequence/swap - 성공")
    @WithTestUser("user@email.com")
    void swapRecordSequenceTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        final int sequence1 = 1;
        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), sequence1));

        final int sequence2 = 2;
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), sequence2));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/sequence/swap")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk());

        Assertions.assertThat(recordRepository.findById(recordEntity1.getId()))
                .isPresent()
                .hasValueSatisfying(recordEntity -> {
                    Assertions.assertThat(recordEntity.getSequence()).isEqualTo(sequence2);
                });

        Assertions.assertThat(recordRepository.findById(recordEntity2.getId()))
                .isPresent()
                .hasValueSatisfying(recordEntity -> {
                    Assertions.assertThat(recordEntity.getSequence()).isEqualTo(sequence1);
                });
    }

    @Test
    @DisplayName("POST /api/v1/records/sequence/swap - 실패 (같은 피드 아이디가 아닌 경우)")
    @WithTestUser("user@email.com")
    void swapRecordSequenceWhenRecordNotSameFeedTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();

        FeedEntity feedEntity1 = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        FeedEntity feedEntity2 = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity1.getUserEntity(), feedEntity1, LocalDateTime.of(2021, 10, 1, 0, 0), 0));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity2.getUserEntity(), feedEntity2, LocalDateTime.of(2021, 10, 1, 0, 0), 1));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/sequence/swap")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records/sequence/swap - 실패 (피드 관리자가 아닌 경우)")
    @WithTestUser("other@email.com")
    void swapRecordSequenceByNotFeedManagerTest() throws Exception {
        //given
        UserEntity writer = userRepository.save(UserEntityFixture.of("test@email.com"));
        UserEntity other = userRepository.findByEmail("other@email.com").get();

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));

        RecordEntity recordEntity1 = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));
        RecordEntity recordEntity2 = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 1));

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(recordEntity1.getId())
                .targetRecordId(recordEntity2.getId())
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/sequence/swap")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(FORBIDDEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/records/sequence/swap - 실패 (존재하지 않는 기록 아이디로 요청)")
    @WithTestUser
    void swapRecordSequenceWhenRecordNotExistingTest() throws Exception {
        //given
        long notExistingRecordId = 0L;

        RecordSequenceSwapRequest request = RecordSequenceSwapRequest.builder()
                .originalRecordId(notExistingRecordId)
                .targetRecordId(notExistingRecordId)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/sequence/swap")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(RECORD_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PUT /api/v1/records - 실패 (올바르지 않은 요청 파라미터)")
    @WithTestUser
    void updateRecordWithInvalidDataTest() throws Exception {
        //given
        RecordUpdateRequest request = RecordUpdateRequest.builder().build();

        //when //then
        mockMvc.perform(
                        put("/api/v1/records/{recordId}", 0L)
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/{recordId} - 실패 (인증되지 않는 사용자)")
    @WithAnonymousUser
    void deleteRecordWithInvalidDataTest() throws Exception {
        //given
        long notExistingRecordId = 0L;
        //when //then
        mockMvc.perform(
                        delete("/api/v1/records/{recordId}", notExistingRecordId)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("DELETE /api/v1/records/{recordId} - 성공")
    @WithTestUser("user@email.com")
    void deleteRecordTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();
        UserEntity commenter1 = UserEntityFixture.of();
        UserEntity commenter2 = UserEntityFixture.of();
        userRepository.saveAll(List.of(commenter1, commenter2));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));
        CommentEntity commentEntity1 = CommentEntityFixture.of(commenter1, recordEntity);
        CommentEntity commentEntity2 = CommentEntityFixture.of(commenter2, recordEntity);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2));

        //when //then
        mockMvc.perform(
                        delete("/api/v1/records/{recordId}", recordEntity.getId())
                )
                .andExpect(status().isOk());

        Assertions.assertThat(commentRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/records/{recordId}/like - 성공 (false 리턴)")
    @WithTestUser("user@email.com")
    void toggleLikeTestWhenUserLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));
        userRecordLikeRepository.save(UserRecordLikeFixture.of(userEntity, recordEntity));

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/{recordId}/like", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.data.liked").value(false));

    }

    @Test
    @DisplayName("POST /api/v1/records/{recordId}/like - 성공 (true 리턴)")
    @WithTestUser("user@email.com")
    void toggleLikeTestWhenUserNotLikeRecord() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(createFeed(userEntity, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2022, 3, 2, 0, 0), 0));

        //when //then
        mockMvc.perform(
                        post("/api/v1/records/{recordId}/like", recordEntity.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/records/{recordId}/comments - 성공")
    @WithTestUser("user@email.com")
    void getRecordCommentsTest() throws Exception {
        //given
        UserEntity writer = userRepository.findByEmail("user@email.com").get();
        UserEntity commenter1 = UserEntityFixture.of();
        UserEntity commenter2 = UserEntityFixture.of();
        userRepository.saveAll(List.of(commenter1, commenter2));

        FeedEntity feedEntity = feedRepository.save(createFeed(writer, LocalDateTime.of(2021, 9, 30, 0, 0), LocalDateTime.of(2021, 10, 2, 0, 0)));
        RecordEntity recordEntity = recordRepository.save(createRecord(feedEntity.getUserEntity(), feedEntity, LocalDateTime.of(2021, 10, 1, 0, 0), 0));
        CommentEntity commentEntity1 = CommentEntityFixture.of(commenter1, recordEntity);
        CommentEntity commentEntity2 = CommentEntityFixture.of(commenter2, recordEntity);
        commentRepository.saveAll(List.of(commentEntity2, commentEntity1));

        //when //then
        mockMvc.perform(
                        get("/api/v1/records/{recordId}/comments", recordEntity.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(2));
    }

    private FeedEntity createFeed(UserEntity userEntity, LocalDateTime startAt, LocalDateTime endAt) {
        return FeedEntity.builder()
                .userEntity(userEntity)
                .name("name")
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    private RecordEntity createRecord(UserEntity userEntity, FeedEntity feedEntity, LocalDateTime date, int sequence) {
        return RecordEntity.builder()
                .userEntity(userEntity)
                .feedEntity(feedEntity)
                .title("record")
                .place("place")
                .longitude("longitude")
                .latitude("latitude")
                .date(date)
                .content("content")
                .weather("weather")
                .transportation("satisfaction")
                .feeling("feeling")
                .sequence(sequence)
                .build();
    }
}