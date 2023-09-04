package world.trecord.controller.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.controller.ApiResponse;
import world.trecord.config.security.CurrentUser;
import world.trecord.service.record.RecordService;
import world.trecord.service.record.request.RecordCreateRequest;
import world.trecord.service.record.request.RecordSequenceSwapRequest;
import world.trecord.service.record.request.RecordUpdateRequest;
import world.trecord.service.record.response.RecordCommentsResponse;
import world.trecord.service.record.response.RecordCreateResponse;
import world.trecord.service.record.response.RecordInfoResponse;
import world.trecord.service.record.response.RecordSequenceSwapResponse;
import world.trecord.service.userrecordlike.UserRecordLikeService;
import world.trecord.service.userrecordlike.response.UserRecordLikeResponse;
import world.trecord.service.users.UserContext;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/records")
public class RecordController {

    private final RecordValidator recordValidator;
    private final RecordService recordService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> getRecordInfo(@PathVariable("recordId") Long recordId, @CurrentUser UserContext userContext) {
        Long viewerId = (userContext != null) ? userContext.getId() : null;
        return ApiResponse.ok(recordService.getRecord(viewerId, recordId));
    }

    @GetMapping("/{recordId}/comments")
    public ApiResponse<RecordCommentsResponse> getRecordComments(@PathVariable("recordId") Long recordId,
                                                                 @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                 @CurrentUser UserContext userContext) {
        Long viewerId = (userContext != null) ? userContext.getId() : null;
        return ApiResponse.ok(recordService.getRecordComments(recordId, viewerId));
    }

    @PostMapping
    public ApiResponse<RecordCreateResponse> createRecord(@RequestBody @Valid RecordCreateRequest recordCreateRequest, @CurrentUser UserContext userContext) throws BindException {
        recordValidator.verify(recordCreateRequest);
        return ApiResponse.ok(recordService.createRecord(userContext.getId(), recordCreateRequest));
    }

    @PostMapping("/swap")
    public ApiResponse<RecordSequenceSwapResponse> swapRecordSequence(@RequestBody @Valid RecordSequenceSwapRequest recordSequenceSwapRequest, @CurrentUser UserContext userContext) {
        return ApiResponse.ok(recordService.swapRecordSequence(userContext.getId(), recordSequenceSwapRequest));
    }

    @PutMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> updateRecord(@PathVariable("recordId") Long recordId, @RequestBody @Valid RecordUpdateRequest recordUpdateRequest, @CurrentUser UserContext userContext) throws BindException {
        recordValidator.verify(recordId, recordUpdateRequest);
        return ApiResponse.ok(recordService.updateRecord(userContext.getId(), recordId, recordUpdateRequest));
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> deleteRecord(@PathVariable("recordId") Long recordId, @CurrentUser UserContext userContext) {
        recordService.deleteRecord(userContext.getId(), recordId);
        return ApiResponse.ok();
    }

    @PostMapping("/{recordId}/like")
    public ApiResponse<UserRecordLikeResponse> toggleLike(@PathVariable("recordId") Long recordId, @CurrentUser UserContext userContext) {
        return ApiResponse.ok(userRecordLikeService.toggleLike(userContext.getId(), recordId));
    }
}