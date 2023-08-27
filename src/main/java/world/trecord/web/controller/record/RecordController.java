package world.trecord.web.controller.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.record.RecordService;
import world.trecord.web.service.record.request.RecordCreateRequest;
import world.trecord.web.service.record.request.RecordUpdateRequest;
import world.trecord.web.service.record.response.RecordCommentsResponse;
import world.trecord.web.service.record.response.RecordCreateResponse;
import world.trecord.web.service.record.response.RecordDeleteResponse;
import world.trecord.web.service.record.response.RecordInfoResponse;
import world.trecord.web.service.userrecordlike.UserRecordLikeService;
import world.trecord.web.service.userrecordlike.response.UserRecordLikeResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/records")
public class RecordController {

    private final RecordValidator recordValidator;
    private final RecordService recordService;
    private final UserRecordLikeService userRecordLikeService;

    @GetMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> getRecordInfo(@PathVariable("recordId") String recordId, @LoginUserId String viewerId) {
        return ApiResponse.ok(recordService.getRecordInfo(Long.valueOf(recordId), viewerId != null ? Long.valueOf(viewerId) : null));
    }

    @GetMapping("/{recordId}/comments")
    public ApiResponse<RecordCommentsResponse> getRecordComments(@PathVariable("recordId") String recordId, @LoginUserId String viewerId) {
        return ApiResponse.ok(recordService.getRecordComments(Long.valueOf(recordId), viewerId != null ? Long.valueOf(viewerId) : null));
    }

    @PostMapping
    public ApiResponse<RecordCreateResponse> createRecord(@RequestBody @Valid RecordCreateRequest recordCreateRequest, @LoginUserId String userId) throws BindException {
        recordValidator.verify(recordCreateRequest);
        return ApiResponse.ok(recordService.createRecord(Long.valueOf(userId), recordCreateRequest));
    }

    @PutMapping("/{recordId}")
    public ApiResponse<RecordInfoResponse> updateRecord(@PathVariable("recordId") Long recordId, @RequestBody @Valid RecordUpdateRequest recordUpdateRequest, @LoginUserId String userId) throws BindException {
        recordValidator.verify(recordId, recordUpdateRequest);
        return ApiResponse.ok(recordService.updateRecord(Long.valueOf(userId), recordId, recordUpdateRequest));
    }

    @DeleteMapping("/{recordId}")
    public ApiResponse<RecordDeleteResponse> deleteRecord(@PathVariable("recordId") Long recordId, @LoginUserId String userId) {
        return ApiResponse.ok(recordService.deleteRecord(Long.valueOf(userId), recordId));
    }

    @PostMapping("/{recordId}/like")
    public ApiResponse<UserRecordLikeResponse> toggleLike(@PathVariable("recordId") Long recordId, @LoginUserId String userId) {
        return ApiResponse.ok(userRecordLikeService.toggleLike(recordId, Long.valueOf(userId)));
    }
}
