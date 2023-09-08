package world.trecord.controller.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.CurrentUser;
import world.trecord.controller.ApiResponse;
import world.trecord.service.record.RecordService;
import world.trecord.service.record.request.RecordCreateRequest;
import world.trecord.service.record.request.RecordSequenceSwapRequest;
import world.trecord.service.record.request.RecordUpdateRequest;
import world.trecord.service.record.response.RecordCommentsResponse;
import world.trecord.service.record.response.RecordCreateResponse;
import world.trecord.service.record.response.RecordInfoResponse;
import world.trecord.service.userrecordlike.UserRecordLikeService;
import world.trecord.service.userrecordlike.response.UserRecordLikeResponse;
import world.trecord.service.users.UserContext;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/records")
public class RecordController {

    private final RecordValidator recordValidator;
    private final RecordService recordService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> getRecordInfo(@PathVariable Long recordId, @CurrentUser UserContext userContext) {
        Optional<Long> viewerId = Optional.ofNullable(userContext).map(UserContext::getId);
        return ApiResponse.ok(recordService.getRecord(viewerId, recordId));
    }

    @GetMapping("/{recordId}/comments")
    public ApiResponse<RecordCommentsResponse> getRecordComments(@PathVariable Long recordId,
                                                                 @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                 @CurrentUser UserContext userContext) {
        Optional<Long> viewerId = Optional.ofNullable(userContext).map(UserContext::getId);
        return ApiResponse.ok(recordService.getRecordComments(viewerId, recordId));
    }

    @PostMapping
    public ApiResponse<RecordCreateResponse> createRecord(@RequestBody @Valid RecordCreateRequest request, @CurrentUser UserContext userContext) throws BindException {
        recordValidator.verify(request);
        return ApiResponse.ok(recordService.createRecord(userContext.getId(), request));
    }

    @PostMapping("/sequence/swap")
    public ApiResponse<Void> swapRecordSequence(@RequestBody @Valid RecordSequenceSwapRequest request, @CurrentUser UserContext userContext) {
        recordService.swapRecordSequence(userContext.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{recordId}")
    public ApiResponse<Void> updateRecord(@PathVariable Long recordId,
                                          @RequestBody @Valid RecordUpdateRequest request,
                                          @CurrentUser UserContext userContext) throws BindException {
        recordValidator.verify(recordId, request);
        recordService.updateRecord(userContext.getId(), recordId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long recordId, @CurrentUser UserContext userContext) {
        recordService.deleteRecord(userContext.getId(), recordId);
        return ApiResponse.ok();
    }

    @PostMapping("/{recordId}/like")
    public ApiResponse<UserRecordLikeResponse> toggleLike(@PathVariable Long recordId, @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userRecordLikeService.toggleLike(userContext.getId(), recordId));
    }
}
