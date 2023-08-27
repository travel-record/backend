package world.trecord.web.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.comment.CommentService;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentCreateResponse;
import world.trecord.web.service.comment.response.CommentDeleteResponse;
import world.trecord.web.service.comment.response.CommentUpdateResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentCreateResponse> createComment(@RequestBody @Valid CommentCreateRequest request, @LoginUserId String userId) {
        return ApiResponse.ok(commentService.createComment(Long.parseLong(userId), request));
    }

    @PutMapping
    public ApiResponse<CommentUpdateResponse> updateComment(@RequestBody @Valid CommentUpdateRequest request, @LoginUserId String userId) {
        return ApiResponse.ok(commentService.updateComment(Long.parseLong(userId), request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<CommentDeleteResponse> deleteComment(@PathVariable("commentId") Long commentId, @LoginUserId String userId) {
        return ApiResponse.ok(commentService.deleteComment(Long.parseLong(userId), commentId));
    }
}
