package world.trecord.web.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.users.UserService;
import world.trecord.web.service.users.request.UserUpdateRequest;
import world.trecord.web.service.users.response.UserCommentsResponse;
import world.trecord.web.service.users.response.UserInfoResponse;
import world.trecord.web.service.users.response.UserRecordLikeListResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<UserInfoResponse> getUserInfo(@LoginUserId String userId) {
        return ApiResponse.ok(userService.getUserInfo(Long.valueOf(userId)));
    }

    @PostMapping
    public ApiResponse<UserInfoResponse> updateUserInfo(@RequestBody @Valid UserUpdateRequest updateRequest, @LoginUserId String userId) {
        return ApiResponse.ok(userService.updateUserInfo(Long.valueOf(userId), updateRequest));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfoByPath(@PathVariable("userId") String userId) {
        return ApiResponse.ok(userService.getUserInfo(Long.valueOf(userId)));
    }

    @GetMapping("/comments")
    public ApiResponse<UserCommentsResponse> getUserComments(@LoginUserId String userId) {
        return ApiResponse.ok(userService.getUserCommentsBy(Long.valueOf(userId)));
    }

    @GetMapping("/likes")
    public ApiResponse<UserRecordLikeListResponse> getUserRecordLikes(@LoginUserId String userId) {
        return ApiResponse.ok(userService.getUserRecordLikeListBy(Long.valueOf(userId)));
    }
}
