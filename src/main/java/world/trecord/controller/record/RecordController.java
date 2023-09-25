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
import world.trecord.config.security.UserContext;
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

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/records")
public class RecordController {

    private final RecordValidator recordValidator;
    private final RecordService recordService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> getRecordInfo(@PathVariable Long recordId, @CurrentContext AccountContext accountContext) {
        Optional<Long> idOpt = Optional.ofNullable(accountContext.getId());
        return ApiResponse.ok(recordService.getRecord(idOpt, recordId));
    }

    @GetMapping("/{recordId}/comments")
    public ApiResponse<Page<RecordCommentResponse>> getRecordComments(@PathVariable Long recordId,
                                                                      @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                                      @CurrentContext AccountContext accountContext) {
        Optional<Long> idOpt = Optional.ofNullable(accountContext.getId());
        return ApiResponse.ok(recordService.getRecordComments(idOpt, recordId, pageable));
    }

    @PostMapping
    public ApiResponse<RecordCreateResponse> createRecord(@RequestBody @Valid RecordCreateRequest request, @CurrentContext UserContext userContext) throws BindException {
        recordValidator.verify(request);
        return ApiResponse.ok(recordService.createRecord(userContext.getId(), request));
    }

    @PostMapping("/sequence/swap")
    public ApiResponse<Void> swapRecordSequence(@RequestBody @Valid RecordSequenceSwapRequest request, @CurrentContext UserContext userContext) {
        recordService.swapRecordSequence(userContext.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{recordId}")
    public ApiResponse<Void> updateRecord(@PathVariable Long recordId,
                                          @RequestBody @Valid RecordUpdateRequest request,
                                          @CurrentContext UserContext userContext) throws BindException {
        recordValidator.verify(recordId, request);
        recordService.updateRecord(userContext.getId(), recordId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long recordId, @CurrentContext UserContext userContext) {
        recordService.deleteRecord(userContext.getId(), recordId);
        return ApiResponse.ok();
    }

    @PostMapping("/{recordId}/like")
    public ApiResponse<UserRecordLikedResponse> toggleLike(@PathVariable Long recordId, @CurrentContext UserContext userContext) {
        return ApiResponse.ok(userRecordLikeService.toggleLike(userContext.getId(), recordId));
    }
}
