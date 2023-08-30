package world.trecord.web.controller.feed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.feed.FeedService;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;
import world.trecord.web.service.feed.response.FeedCreateResponse;
import world.trecord.web.service.feed.response.FeedInfoResponse;
import world.trecord.web.service.feed.response.FeedListResponse;
import world.trecord.web.service.feed.response.FeedUpdateResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedController {

    private final FeedService feedService;
    private final FeedValidator feedValidator;

    @GetMapping
    public ApiResponse<FeedListResponse> getFeedList(@LoginUserId String userId) {
        return ApiResponse.ok(feedService.getFeedList(Long.valueOf(userId)));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable("feedId") Long feedId, @LoginUserId String viewerId) {
        return ApiResponse.ok(feedService.getFeed(feedId, viewerId != null ? Long.parseLong(viewerId) : null));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Valid FeedCreateRequest feedCreateRequest, @LoginUserId String userId) throws BindException {
        feedValidator.verify(feedCreateRequest);
        return ApiResponse.ok(feedService.createFeed(Long.valueOf(userId), feedCreateRequest));
    }

    @PutMapping("/{feedId}")
    public ApiResponse<FeedUpdateResponse> updateFeed(@PathVariable("feedId") Long feedId, @RequestBody @Valid FeedUpdateRequest feedUpdateRequest, @LoginUserId String userId) throws BindException {
        feedValidator.verify(feedUpdateRequest);
        return ApiResponse.ok(feedService.updateFeed(Long.valueOf(userId), feedId, feedUpdateRequest));
    }

    @DeleteMapping("/{feedId}")
    public ApiResponse<Void> deleteFeed(@PathVariable("feedId") Long feedId, @LoginUserId String userId) {
        feedService.deleteFeed(Long.valueOf(userId), feedId);
        return ApiResponse.ok();
    }
}
