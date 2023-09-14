package world.trecord.controller.feed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import world.trecord.service.feed.response.FeedRecordsResponse;
import world.trecord.service.feedcontributor.FeedContributorService;
import world.trecord.service.feedcontributor.request.FeedInviteRequest;
import world.trecord.service.users.UserContext;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedController {

    private final FeedService feedService;
    private final FeedContributorService feedContributorService;
    private final FeedValidator feedValidator;

    @GetMapping
    public ApiResponse<FeedListResponse> getFeedList(@PageableDefault(sort = "startAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                     @CurrentUser UserContext userContext) {
        return ApiResponse.ok(feedService.getFeedList(userContext.getId()));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable Long feedId,
                                                 @CurrentUser UserContext userContext) {
        Optional<Long> viewerId = Optional.ofNullable(userContext).map(UserContext::getId);
        return ApiResponse.ok(feedService.getFeed(viewerId, feedId));
    }

    @GetMapping("/{feedId}/records")
    public ApiResponse<Page<FeedRecordsResponse>> getFeedRecords(@PathVariable Long feedId,
                                                                 @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(feedService.getFeedRecords(feedId, pageable));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Valid FeedCreateRequest request,
                                                      @CurrentUser UserContext userContext) throws BindException {
        feedValidator.verify(request);
        return ApiResponse.ok(feedService.createFeed(userContext.getId(), request));
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
    public ApiResponse<Void> deleteFeed(@PathVariable Long feedId,
                                        @CurrentUser UserContext userContext) {
        feedService.deleteFeed(userContext.getId(), feedId);
        return ApiResponse.ok();
    }

    @PostMapping("/{feedId}/contributors/invite")
    public ApiResponse<Void> inviteUser(@PathVariable Long feedId,
                                        @RequestBody @Valid FeedInviteRequest request,
                                        @CurrentUser UserContext userContext) {
        feedContributorService.inviteUserToFeed(userContext.getId(), feedId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{feedId}/contributors/{contributorId}")
    public ApiResponse<Void> expelUser(@PathVariable Long feedId,
                                       @PathVariable Long contributorId,
                                       @CurrentUser UserContext userContext) {
        feedContributorService.expelUserFromFeed(userContext.getId(), contributorId, feedId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{feedId}/contributors/leave")
    public ApiResponse<Void> leaveFeed(@PathVariable Long feedId,
                                       @CurrentUser UserContext userContext) {
        feedContributorService.leaveFeed(userContext.getId(), feedId);
        return ApiResponse.ok();
    }
}
