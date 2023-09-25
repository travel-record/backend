package world.trecord.controller.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.AccountContext;
import world.trecord.config.security.CurrentContext;
import world.trecord.controller.ApiResponse;
import world.trecord.dto.record.request.RecordCreateRequest;
import world.trecord.dto.record.request.RecordSequenceSwapRequest;
import world.trecord.dto.record.request.RecordUpdateRequest;
import world.trecord.dto.record.response.RecordCommentResponse;
import world.trecord.dto.record.response.RecordCreateResponse;
import world.trecord.dto.record.response.RecordInfoResponse;
import world.trecord.dto.userrecordlike.response.UserRecordLikedResponse;
import world.trecord.service.record.RecordService;
import world.trecord.service.userrecordlike.UserRecordLikeService;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/records")
public class RecordController {

    private final RecordValidator recordValidator;
    private final RecordService recordService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> getRecordInfo(@PathVariable Long recordId,
                                                         @CurrentContext AccountContext accountContext) {
        return ApiResponse.ok(recordService.getRecord(accountContext.getId(), recordId));
    }

    @GetMapping("/{recordId}/comments")
    public ApiResponse<Page<RecordCommentResponse>> getRecordComments(@PathVariable Long recordId,
                                                                      @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                      @CurrentContext AccountContext accountContext) {
        return ApiResponse.ok(recordService.getRecordComments(accountContext.getId(), recordId, pageable));
    }

    @PostMapping
    public ApiResponse<RecordCreateResponse> createRecord(@RequestBody @Valid RecordCreateRequest request,
                                                          @CurrentContext AccountContext accountContext) throws BindException {
        recordValidator.verify(request);
        return ApiResponse.ok(recordService.createRecord(accountContext.getId(), request));
    }

    @PostMapping("/sequence/swap")
    public ApiResponse<Void> swapRecordSequence(@RequestBody @Valid RecordSequenceSwapRequest request,
                                                @CurrentContext AccountContext accountContext) {
        recordService.swapRecordSequence(accountContext.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{recordId}")
    public ApiResponse<Void> updateRecord(@PathVariable Long recordId,
                                          @RequestBody @Valid RecordUpdateRequest request,
                                          @CurrentContext AccountContext accountContext) throws BindException {
        recordValidator.verify(recordId, request);
        recordService.updateRecord(accountContext.getId(), recordId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long recordId,
                                          @CurrentContext AccountContext accountContext) {
        recordService.deleteRecord(accountContext.getId(), recordId);
        return ApiResponse.ok();
    }

    @PostMapping("/{recordId}/like")
    public ApiResponse<UserRecordLikedResponse> toggleLike(@PathVariable Long recordId,
                                                           @CurrentContext AccountContext accountContext) {
        return ApiResponse.ok(userRecordLikeService.toggleLike(accountContext.getId(), recordId));
    }
}
