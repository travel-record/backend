package world.trecord.web.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.LoginUserId;
import world.trecord.web.service.comment.CommentService;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentUpdateResponse;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ApiResponse<Void> createComment(@RequestBody @Valid CommentCreateRequest request, @LoginUserId String userId) {
        commentService.createComment(Long.parseLong(userId), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentUpdateResponse> updateComment(@PathVariable("commentId") Long commentId, @RequestBody @Valid CommentUpdateRequest request, @LoginUserId String userId) {
        return ApiResponse.ok(commentService.updateComment(Long.parseLong(userId), commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable("commentId") Long commentId, @LoginUserId String userId) {
        commentService.deleteComment(Long.parseLong(userId), commentId);
        return ApiResponse.ok();
    }
}
