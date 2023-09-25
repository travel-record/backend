package world.trecord.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.CurrentContext;
import world.trecord.config.security.UserContext;
import world.trecord.controller.ApiResponse;
import world.trecord.dto.comment.response.UserCommentResponse;
import world.trecord.dto.feedcontributor.response.UserFeedContributorListResponse;
import world.trecord.dto.userrecordlike.response.UserRecordLikeResponse;
import world.trecord.dto.users.request.UserUpdateRequest;
import world.trecord.dto.users.response.UserInfoResponse;
import world.trecord.service.comment.CommentService;
import world.trecord.service.feedcontributor.FeedContributorService;
import world.trecord.service.userrecordlike.UserRecordLikeService;
import world.trecord.service.users.UserService;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {

    private final UserService userService;
    private final CommentService commentService;
    private final UserRecordLikeService userRecordLikeService;
    private final FeedContributorService feedContributorService;

    @GetMapping
    public ApiResponse<UserInfoResponse> getUser(@CurrentContext UserContext userContext) {
        return ApiResponse.ok(userService.getUser(userContext.getId()));
    }

    @PostMapping
    public ApiResponse<UserInfoResponse> updateUser(@RequestBody @Valid UserUpdateRequest request,
                                                    @CurrentContext UserContext userContext) {
        return ApiResponse.ok(userService.updateUser(userContext.getId(), request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserByUserId(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUser(userId));
    }

    @GetMapping("/search")
    public ApiResponse<UserInfoResponse> searchUser(@RequestParam(name = "q") String keyword,
                                                    @CurrentContext UserContext userContext) {
        return ApiResponse.ok(userService.searchUser(userContext.getId(), keyword));
    }

    @GetMapping("/comments")
    public ApiResponse<Page<UserCommentResponse>> getUserComments(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                                  @CurrentContext UserContext userContext) {
        return ApiResponse.ok(commentService.getUserComments(userContext.getId(), pageable));
    }

    @GetMapping("/likes")
    public ApiResponse<Page<UserRecordLikeResponse>> getUserRecordLikes(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                                        @CurrentContext UserContext userContext) {
        return ApiResponse.ok(userRecordLikeService.getUserRecordLikeList(userContext.getId(), pageable));
    }

    @GetMapping("/invited")
    public ApiResponse<Page<UserFeedContributorListResponse>> getUserParticipatingFeeds(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                                                        @CurrentContext UserContext userContext) {
        return ApiResponse.ok(feedContributorService.getUserParticipatingFeeds(userContext.getId(), pageable));
    }
}
