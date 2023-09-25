package world.trecord.controller.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.AccountContext;
import world.trecord.config.security.CurrentContext;
import world.trecord.config.security.UserContext;
import world.trecord.controller.ApiResponse;
import world.trecord.dto.feed.request.FeedCreateRequest;
import world.trecord.dto.feed.request.FeedUpdateRequest;
import world.trecord.dto.feed.response.FeedCreateResponse;
import world.trecord.dto.feed.response.FeedInfoResponse;
import world.trecord.dto.feed.response.FeedListResponse;
import world.trecord.dto.feed.response.FeedRecordsResponse;
import world.trecord.dto.feedcontributor.request.FeedInviteRequest;
import world.trecord.service.feed.FeedService;
import world.trecord.service.feedcontributor.FeedContributorService;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedController {

    private final FeedService feedService;
    private final FeedContributorService feedContributorService;
    private final FeedValidator feedValidator;

    @GetMapping
    public ApiResponse<Page<FeedListResponse>> getFeedList(@PageableDefault(sort = "startAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                           @CurrentContext UserContext userContext) {
        return ApiResponse.ok(feedService.getFeedList(userContext.getId(), pageable));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable Long feedId,
                                                 @CurrentContext AccountContext accountContext) {
        Optional<Long> idOpt = Optional.ofNullable(accountContext.getId());
        return ApiResponse.ok(feedService.getFeed(idOpt, feedId));
    }

    @GetMapping("/{feedId}/records")
    public ApiResponse<Page<FeedRecordsResponse>> getFeedRecords(@PathVariable Long feedId,
                                                                 @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(feedService.getFeedRecords(feedId, pageable));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Validated FeedCreateRequest request,
                                                      @CurrentContext UserContext userContext) throws BindException {
        feedValidator.verify(request);
        return ApiResponse.ok(feedService.createFeed(userContext.getId(), request));
    }

    @PutMapping("/{feedId}")
    public ApiResponse<Void> updateFeed(@PathVariable Long feedId,
                                        @RequestBody @Validated FeedUpdateRequest request,
                                        @CurrentContext UserContext userContext) throws BindException {
        feedValidator.verify(request);
        feedService.updateFeed(userContext.getId(), feedId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{feedId}")
    public ApiResponse<Void> deleteFeed(@PathVariable Long feedId,
                                        @CurrentContext UserContext userContext) {
        feedService.deleteFeed(userContext.getId(), feedId);
        return ApiResponse.ok();
    }

    @PostMapping("/{feedId}/contributors/invite")
    public ApiResponse<Void> inviteUser(@PathVariable Long feedId,
                                        @RequestBody @Validated FeedInviteRequest request,
                                        @CurrentContext UserContext userContext) {
        feedContributorService.inviteUserToFeed(userContext.getId(), feedId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{feedId}/contributors/{contributorId}")
    public ApiResponse<Void> expelUser(@PathVariable Long feedId,
                                       @PathVariable Long contributorId,
                                       @CurrentContext UserContext userContext) {
        feedContributorService.expelUserFromFeed(userContext.getId(), contributorId, feedId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{feedId}/contributors/leave")
    public ApiResponse<Void> leaveFeed(@PathVariable Long feedId,
                                       @CurrentContext UserContext userContext) {
        feedContributorService.leaveFeed(userContext.getId(), feedId);
        return ApiResponse.ok();
    }
}
