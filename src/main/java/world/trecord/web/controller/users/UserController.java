package world.trecord.web.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.comment.CommentService;
import world.trecord.web.service.comment.response.UserCommentsResponse;
import world.trecord.web.service.userrecordlike.UserRecordLikeService;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeListResponse;
import world.trecord.web.service.users.UserContext;
import world.trecord.web.service.users.UserService;
import world.trecord.web.service.users.request.UserUpdateRequest;
import world.trecord.web.service.users.response.UserInfoResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {

    private final UserService userService;
    private final CommentService commentService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping
    public ApiResponse<UserInfoResponse> getUser(@CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.getUser(userContext.getId()));
    }

    @PostMapping
    public ApiResponse<UserInfoResponse> updateUser(@RequestBody @Valid UserUpdateRequest updateRequest, @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.updateUser(userContext.getId(), updateRequest));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserByUserId(@PathVariable("userId") Long userId) {
        return ApiResponse.ok(userService.getUser(userId));
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
}
