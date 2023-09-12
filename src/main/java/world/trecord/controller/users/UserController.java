package world.trecord.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.CurrentUser;
import world.trecord.controller.ApiResponse;
import world.trecord.service.comment.CommentService;
import world.trecord.service.comment.response.UserCommentsResponse;
import world.trecord.service.feedcontributor.FeedContributorService;
import world.trecord.service.feedcontributor.response.UserFeedContributorListResponse;
import world.trecord.service.userrecordlike.UserRecordLikeService;
import world.trecord.service.userrecordlike.response.UserRecordLikeListResponse;
import world.trecord.service.users.UserContext;
import world.trecord.service.users.UserService;
import world.trecord.service.users.request.UserUpdateRequest;
import world.trecord.service.users.response.UserInfoResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {

    private final UserService userService;
    private final FeedContributorService feedContributorService;
    private final CommentService commentService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping
    public ApiResponse<UserInfoResponse> getUser(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.getUser(userContext.getId()));
    }

    @PostMapping
    public ApiResponse<UserInfoResponse> updateUser(@RequestBody @Valid UserUpdateRequest request,
                                                    @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.updateUser(userContext.getId(), request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserByUserId(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUser(userId));
    }

    @GetMapping("/search")
    public ApiResponse<UserInfoResponse> searchUser(@RequestParam(name = "q") String keyword) {
        return ApiResponse.ok(userService.searchUser(keyword));
    }

    @GetMapping("/comments")
    public ApiResponse<UserCommentsResponse> getUserComments(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                             @CurrentUser UserContext userContext) {
        return ApiResponse.ok(commentService.getUserComments(userContext.getId()));
    }

    @GetMapping("/likes")
    public ApiResponse<UserRecordLikeListResponse> getUserRecordLikes(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                      @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userRecordLikeService.getUserRecordLikeList(userContext.getId()));
    }

    @GetMapping("/invited")
    public ApiResponse<Page<UserFeedContributorListResponse>> getFeedInvited(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                                                                             @CurrentUser UserContext userContext) {
        return ApiResponse.ok(feedContributorService.getUserFeedContributors(userContext.getId(), pageable));
    }
}
