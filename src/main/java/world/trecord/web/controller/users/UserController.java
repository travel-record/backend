package world.trecord.web.controller.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.users.UserContext;
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
        return ApiResponse.ok(userService.getUserComments(userContext.getId()));
    }

    @GetMapping("/likes")
    public ApiResponse<UserRecordLikeListResponse> getUserRecordLikes(@PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                      @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userService.getUserRecordLikeList(userContext.getId()));
    }
}
