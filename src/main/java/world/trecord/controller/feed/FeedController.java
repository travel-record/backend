package world.trecord.controller.feed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.CurrentUser;
import world.trecord.controller.ApiResponse;
import world.trecord.service.feed.FeedService;
import world.trecord.service.feed.request.FeedCreateRequest;
import world.trecord.service.feed.request.FeedUpdateRequest;
import world.trecord.service.feed.response.FeedCreateResponse;
import world.trecord.service.feed.response.FeedInfoResponse;
import world.trecord.service.feed.response.FeedListResponse;
import world.trecord.service.invitation.InvitationService;
import world.trecord.service.invitation.request.FeedExpelRequest;
import world.trecord.service.invitation.request.FeedInviteRequest;
import world.trecord.service.users.UserContext;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedController {

    private final FeedService feedService;
    private final InvitationService invitationService;
    private final FeedValidator feedValidator;

    @GetMapping
    public ApiResponse<FeedListResponse> getFeedList(@PageableDefault(sort = "startAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                     @CurrentUser UserContext userContext) {
        return ApiResponse.ok(feedService.getFeedList(userContext.getId()));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable Long feedId, @CurrentUser UserContext userContext) {
        Optional<Long> viewerId = Optional.ofNullable(userContext).map(UserContext::getId);
        return ApiResponse.ok(feedService.getFeed(viewerId, feedId));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Valid FeedCreateRequest request, @CurrentUser UserContext userContext) throws BindException {
        feedValidator.verify(request);
        return ApiResponse.ok(feedService.createFeed(userContext.getId(), request));
    }

    @PostMapping("/{feedId}/invite")
    public ApiResponse<Void> inviteUser(@PathVariable Long feedId,
                                        @RequestBody @Valid FeedInviteRequest request,
                                        @CurrentUser UserContext userContext) {
        invitationService.inviteUser(userContext.getId(), feedId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/{feedId}/expel")
    public ApiResponse<Void> expelUser(@PathVariable Long feedId,
                                       @RequestBody @Valid FeedExpelRequest request,
                                       @CurrentUser UserContext userContext) {
        invitationService.expelUser(userContext.getId(), feedId, request);
        return ApiResponse.ok();
    }

    @PutMapping("/{feedId}")
    public ApiResponse<Void> updateFeed(@PathVariable Long feedId,
                                        @RequestBody @Valid FeedUpdateRequest request,
                                        @CurrentUser UserContext userContext) throws BindException {
        feedValidator.verify(request);
        feedService.updateFeed(userContext.getId(), feedId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{feedId}")
    public ApiResponse<Void> deleteFeed(@PathVariable Long feedId, @CurrentUser UserContext userContext) {
        feedService.deleteFeed(userContext.getId(), feedId);
        return ApiResponse.ok();
    }
}
