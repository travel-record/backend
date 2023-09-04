package world.trecord.controller.feed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.controller.ApiResponse;
import world.trecord.config.security.CurrentUser;
import world.trecord.service.feed.FeedService;
import world.trecord.service.feed.request.FeedCreateRequest;
import world.trecord.service.feed.request.FeedUpdateRequest;
import world.trecord.service.feed.response.FeedCreateResponse;
import world.trecord.service.feed.response.FeedInfoResponse;
import world.trecord.service.feed.response.FeedListResponse;
import world.trecord.service.feed.response.FeedUpdateResponse;
import world.trecord.service.users.UserContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedController {

    private final FeedService feedService;
    private final FeedValidator feedValidator;

    @GetMapping
    public ApiResponse<FeedListResponse> getFeedList(@PageableDefault(sort = "startAt", direction = Sort.Direction.DESC) Pageable pageable,
                                                     @CurrentUser UserContext userContext) {
        return ApiResponse.ok(feedService.getFeedList(userContext.getId()));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable("feedId") Long feedId, @CurrentUser UserContext userContext) {
        Long viewerId = (userContext != null) ? userContext.getId() : null;
        return ApiResponse.ok(feedService.getFeed(viewerId, feedId));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Valid FeedCreateRequest feedCreateRequest, @CurrentUser UserContext userContext) throws BindException {
        feedValidator.verify(feedCreateRequest);
        return ApiResponse.ok(feedService.createFeed(userContext.getId(), feedCreateRequest));
    }

    @PutMapping("/{feedId}")
    public ApiResponse<FeedUpdateResponse> updateFeed(@PathVariable("feedId") Long feedId,
                                                      @RequestBody @Valid FeedUpdateRequest feedUpdateRequest,
                                                      @CurrentUser UserContext userContext) throws BindException {
        feedValidator.verify(feedUpdateRequest);
        return ApiResponse.ok(feedService.updateFeed(userContext.getId(), feedId, feedUpdateRequest));
    }

    @DeleteMapping("/{feedId}")
    public ApiResponse<Void> deleteFeed(@PathVariable("feedId") Long feedId, @CurrentUser UserContext userContext) {
        feedService.deleteFeed(userContext.getId(), feedId);
        return ApiResponse.ok();
    }
}
