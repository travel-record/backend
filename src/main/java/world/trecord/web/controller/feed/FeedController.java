package world.trecord.web.controller.feed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.feed.FeedService;
import world.trecord.web.service.feed.request.FeedCreateRequest;
import world.trecord.web.service.feed.request.FeedDeleteRequest;
import world.trecord.web.service.feed.request.FeedUpdateRequest;
import world.trecord.web.service.feed.response.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/feeds")
public class FeedController {

    private final FeedService feedService;
    private final FeedValidator feedValidator;

    @GetMapping
    public ApiResponse<FeedListResponse> getFeedList(@LoginUserId String userId) {
        return ApiResponse.ok(feedService.getFeedListBy(Long.valueOf(userId)));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable("feedId") Long feedId, @LoginUserId String viewerId) {
        return ApiResponse.ok(feedService.getFeedBy(feedId, Long.parseLong(viewerId)));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Valid FeedCreateRequest feedCreateRequest, @LoginUserId String userId) throws BindException {
        feedValidator.verify(feedCreateRequest);
        return ApiResponse.ok(feedService.createFeed(Long.valueOf(userId), feedCreateRequest));
    }

    @PutMapping
    public ApiResponse<FeedUpdateResponse> updateFeed(@RequestBody @Valid FeedUpdateRequest feedUpdateRequest, @LoginUserId String userId) throws BindException {
        feedValidator.verify(feedUpdateRequest);
        return ApiResponse.ok(feedService.updateFeed(Long.valueOf(userId), feedUpdateRequest));
    }

    @DeleteMapping
    public ApiResponse<FeedDeleteResponse> deleteFeed(@RequestBody @Valid FeedDeleteRequest feedDeleteRequest, @LoginUserId String userId) {
        return ApiResponse.ok(feedService.deleteFeed(Long.valueOf(userId), feedDeleteRequest.getId()));
    }
}
