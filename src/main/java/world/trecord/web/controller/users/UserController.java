package world.trecord.web.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import world.trecord.domain.users.UserEntity;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
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
    public ApiResponse<UserInfoResponse> getUserInfo(@CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(userService.getUserInfo(userEntity.getId()));
    }

    @PostMapping
    public ApiResponse<UserInfoResponse> updateUserInfo(@RequestBody @Valid UserUpdateRequest updateRequest, @CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(userService.updateUserInfo(userEntity.getId(), updateRequest));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUserInfoByPath(@PathVariable("userId") Long userId) {
        return ApiResponse.ok(userService.getUserInfo(userId));
    }

    @GetMapping("/comments")
    public ApiResponse<UserCommentsResponse> getUserComments(@CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(userService.getUserCommentsBy(userEntity.getId()));
    }

    @GetMapping("/likes")
    public ApiResponse<UserRecordLikeListResponse> getUserRecordLikes(@CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(userService.getUserRecordLikeListBy(userEntity.getId()));
    }
}
