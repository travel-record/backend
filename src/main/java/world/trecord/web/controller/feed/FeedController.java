package world.trecord.web.controller.feed;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.domain.users.UserEntity;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
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
    public ApiResponse<FeedListResponse> getFeedList(@CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(feedService.getFeedList(userEntity.getId()));
    }

    @GetMapping("/{feedId}")
    public ApiResponse<FeedInfoResponse> getFeed(@PathVariable("feedId") Long feedId, @CurrentUser UserEntity userEntity) {
        Long viewerId = (userEntity != null) ? userEntity.getId() : null;
        return ApiResponse.ok(feedService.getFeed(viewerId, feedId));
    }

    @PostMapping
    public ApiResponse<FeedCreateResponse> createFeed(@RequestBody @Valid FeedCreateRequest feedCreateRequest, @CurrentUser UserEntity userEntity) throws BindException {
        feedValidator.verify(feedCreateRequest);
        return ApiResponse.ok(feedService.createFeed(userEntity.getId(), feedCreateRequest));
    }

    @PutMapping("/{feedId}")
    public ApiResponse<FeedUpdateResponse> updateFeed(@PathVariable("feedId") Long feedId, @RequestBody @Valid FeedUpdateRequest feedUpdateRequest, @CurrentUser UserEntity userEntity) throws BindException {
        feedValidator.verify(feedUpdateRequest);
        return ApiResponse.ok(feedService.updateFeed(userEntity.getId(), feedId, feedUpdateRequest));
    }

    @DeleteMapping("/{feedId}")
    public ApiResponse<Void> deleteFeed(@PathVariable("feedId") Long feedId, @CurrentUser UserEntity userEntity) {
        feedService.deleteFeed(userEntity.getId(), feedId);
        return ApiResponse.ok();
    }
}
