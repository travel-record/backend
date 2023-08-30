package world.trecord.web.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import world.trecord.domain.users.UserEntity;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
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
    public ApiResponse<Void> createComment(@RequestBody @Valid CommentCreateRequest request, @CurrentUser UserEntity userEntity) {
        commentService.createComment(userEntity.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentUpdateResponse> updateComment(@PathVariable("commentId") Long commentId, @RequestBody @Valid CommentUpdateRequest request, @CurrentUser UserEntity userEntity) {
        return ApiResponse.ok(commentService.updateComment(userEntity.getId(), commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable("commentId") Long commentId, @CurrentUser UserEntity userEntity) {
        commentService.deleteComment(userEntity.getId(), commentId);
        return ApiResponse.ok();
    }
}
