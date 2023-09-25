package world.trecord.controller.users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.transaction.annotation.Transactional;
import world.trecord.domain.comment.CommentEntity;
import world.trecord.domain.feed.FeedEntity;
import world.trecord.domain.feedcontributor.FeedContributorEntity;
import world.trecord.domain.record.RecordEntity;
import world.trecord.domain.userrecordlike.UserRecordLikeEntity;
import world.trecord.domain.users.UserEntity;
import world.trecord.dto.users.request.UserUpdateRequest;
import world.trecord.infra.fixture.*;
import world.trecord.infra.support.WithTestUser;
import world.trecord.infra.test.AbstractMockMvcTest;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static world.trecord.exception.CustomExceptionError.*;

@Transactional
class UserControllerTest extends AbstractMockMvcTest {

    @Test
    @DisplayName("GET /api/v1/users - 성공")
    @WithTestUser("user@email.com")
    void getUserInfoTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users - 실패 (미인증 사용자)")
    @WithAnonymousUser
    void getUserInfoWithNotExistingTokenTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("POST /api/v1/users - 성공")
    @WithTestUser("user@email.com")
    void updateUserInfoTest() throws Exception {
        //given
        String nickname = "changed nickname";
        String imageUrl = "changed image url";
        String introduction = "change introduction";

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("POST /api/v1/users - 실패 (이미 등록된 닉네임)")
    @WithTestUser("user@email.com")
    void updateUserInfoWithExistingNicknameTest() throws Exception {
        //given
        String duplicatedNickname = "duplicate nickname";
        UserEntity userEntity = UserEntityFixture.of("test@email.com", duplicatedNickname);
        userRepository.save(userEntity);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .nickname(duplicatedNickname)
                .build();

        //when //then
        mockMvc.perform(
                        post("/api/v1/users")
                                .contentType(APPLICATION_JSON)
                                .content(body(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(NICKNAME_DUPLICATED.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - 성공")
    @WithAnonymousUser
    void getUserInfoByUserIdTest() throws Exception {
        //given
        String email = "test@email.com";
        String nickname = "nickname";
        String imageUrl = "http://localhost/pictures";
        String introduction = "hello";

        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .nickname(nickname)
                .imageUrl(imageUrl)
                .introduction(introduction)
                .build();

        UserEntity saveUser = userRepository.save(userEntity);

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/{userId}", saveUser.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value(nickname))
                .andExpect(jsonPath("$.data.imageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.introduction").value(introduction));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - 실패 (존재하지 않는 사용자 아이디)")
    @WithTestUser
    void getUserInfoByUserIdWithTest() throws Exception {
        //given
        long notExistingUserId = 0L;

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/{userId}", notExistingUserId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/comments - 성공")
    @WithTestUser("user@email.com")
    void getUserCommentsTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2));

        CommentEntity commentEntity1 = CommentEntityFixture.of(userEntity, recordEntity1);
        CommentEntity commentEntity2 = CommentEntityFixture.of(userEntity, recordEntity2);
        CommentEntity commentEntity3 = CommentEntityFixture.of(userEntity, recordEntity2);
        CommentEntity commentEntity4 = CommentEntityFixture.of(userEntity, recordEntity1);
        commentRepository.saveAll(List.of(commentEntity1, commentEntity2, commentEntity3, commentEntity4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/users/comments - 실패 (유효하지 않은 토큰)")
    void getUserCommentsWithNotExistingUserIdTest() throws Exception {
        //given
        String invalidToken = "-1";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/comments - 실패 (토큰 없이)")
    @WithAnonymousUser
    void getUserCommentsWithoutExistingUserIdTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/comments")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/likes - 성공")
    @WithTestUser("user@email.com")
    void getUserRecordLikesTest() throws Exception {
        //given
        UserEntity userEntity = userRepository.findByEmail("user@email.com").get();
        FeedEntity feedEntity = feedRepository.save(FeedEntityFixture.of(userEntity));

        RecordEntity recordEntity1 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity2 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity3 = RecordEntityFixture.of(feedEntity);
        RecordEntity recordEntity4 = RecordEntityFixture.of(feedEntity);
        recordRepository.saveAll(List.of(recordEntity1, recordEntity2, recordEntity3, recordEntity4));

        UserRecordLikeEntity userRecordLikeEntity1 = UserRecordLikeFixture.of(userEntity, recordEntity1);
        UserRecordLikeEntity userRecordLikeEntity2 = UserRecordLikeFixture.of(userEntity, recordEntity4);
        userRecordLikeRepository.saveAll(List.of(userRecordLikeEntity1, userRecordLikeEntity2));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/likes")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.size()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/users/likes - 실패 (올바르지 않은 토큰)")
    void getUserCommentsWithInvalidTokenIdTest() throws Exception {
        //given
        String invalidToken = "-1";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/likes")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/likes - 실패 (토큰 없이)")
    @WithAnonymousUser
    void getUserCommentsWithoutTokenIdTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/likes")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/search?q= - 성공(검색 키워드에 해당하는 유저 존재할 경우)")
    @WithTestUser
    void searchUserTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntityFixture.of("test1@email.com", "김박김");
        UserEntity userEntity2 = UserEntityFixture.of("test2@email.com", "이이이");
        UserEntity userEntity3 = UserEntityFixture.of("test3@email.com", "김박박");
        UserEntity userEntity4 = UserEntityFixture.of("test4@email.com", "김이박");
        UserEntity userEntity5 = UserEntityFixture.of("test5@email.com", "박이김");

        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3, userEntity4, userEntity5));

        String keyword = "이이이";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/search")
                                .param("q", keyword)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userEntity2.getId()));
    }

    @Test
    @DisplayName("GET /api/v1/users/search?q= - 성공(검색 키워드에 해당하는 유저 존재하지 않는 경우)")
    @WithTestUser
    void searchUserWhenKeywordNotMatchedTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntityFixture.of("test1@email.com", "김박김");
        UserEntity userEntity2 = UserEntityFixture.of("test2@email.com", "이이이");
        UserEntity userEntity3 = UserEntityFixture.of("test3@email.com", "김박박");
        UserEntity userEntity4 = UserEntityFixture.of("test4@email.com", "김이박");
        UserEntity userEntity5 = UserEntityFixture.of("test5@email.com", "박이김");

        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3, userEntity4, userEntity5));

        String keyword = "김박김김";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/search")
                                .param("q", keyword)
                                .header(AUTHORIZATION, token(userEntity1.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/users/search?q= - 성공 (자신의 닉네임으로 검색한 경우)")
    void searchUserWhenKeywordIsSelfNicknameTest() throws Exception {
        //given
        UserEntity userEntity1 = UserEntityFixture.of("test1@email.com", "김박김");
        UserEntity userEntity2 = UserEntityFixture.of("test2@email.com", "이이이");
        UserEntity userEntity3 = UserEntityFixture.of("test3@email.com", "김박박");
        UserEntity userEntity4 = UserEntityFixture.of("test4@email.com", "김이박");
        UserEntity userEntity5 = UserEntityFixture.of("test5@email.com", "박이김");

        userRepository.saveAll(List.of(userEntity1, userEntity2, userEntity3, userEntity4, userEntity5));

        String keyword = "김박김";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/search")
                                .param("q", keyword)
                                .header(AUTHORIZATION, token(userEntity1.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/users/search?q= - 실패(쿼리 파라미터 없을때)")
    @WithTestUser
    void searchUserWithEmptyQueryTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/search")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_ARGUMENT.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/search?q= - 실패(유효하지 않은 토큰으로 요청한 경우)")
    void searchUserWhenAuthFailedTest() throws Exception {
        //given
        String invalidToken = "invalidToken";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/search")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/invited - 성공")
    @WithTestUser("invited@email.com")
    void getUserParticipatingFeedsTest() throws Exception {
        //given
        UserEntity owner = userRepository.save(UserEntityFixture.of("owner@email.com", "owner"));
        UserEntity invitedUser = userRepository.findByEmail("invited@email.com").get();

        FeedEntity feedEntity1 = FeedEntityFixture.of(owner);
        FeedEntity feedEntity2 = FeedEntityFixture.of(owner);
        FeedEntity feedEntity3 = FeedEntityFixture.of(owner);
        FeedEntity feedEntity4 = FeedEntityFixture.of(owner);
        feedRepository.saveAll(List.of(feedEntity1, feedEntity2, feedEntity3, feedEntity4));

        FeedContributorEntity feedContributor1 = FeedContributorFixture.of(invitedUser, feedEntity1);
        FeedContributorEntity feedContributor2 = FeedContributorFixture.of(invitedUser, feedEntity2);
        FeedContributorEntity feedContributor3 = FeedContributorFixture.of(invitedUser, feedEntity3);
        FeedContributorEntity feedContributor4 = FeedContributorFixture.of(invitedUser, feedEntity4);
        feedContributorRepository.saveAll(List.of(feedContributor1, feedContributor2, feedContributor3, feedContributor4));

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/invited")
                                .header(AUTHORIZATION, token(invitedUser.getId()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/users/invited - 성공 (초대된 피드가 없는 경우)")
    @WithTestUser
    void getUserParticipatingFeedsWhenFeedsEmptyTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/invited")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/users/invited - 실패 (유효하지 않은 토큰으로 요청)")
    void getUserParticipatingFeedsWithInvalidTokenTest() throws Exception {
        //given
        String invalidToken = "invalid token";

        //when //then
        mockMvc.perform(
                        get("/api/v1/users/invited")
                                .header(AUTHORIZATION, invalidToken)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }

    @Test
    @DisplayName("GET /api/v1/users/invited - 실패 (토큰없이 요청)")
    @WithAnonymousUser
    void getUserParticipatingFeedsWithoutTokenTest() throws Exception {
        //when //then
        mockMvc.perform(
                        get("/api/v1/users/invited")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_TOKEN.code()));
    }
}