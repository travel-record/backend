package world.trecord.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.account.AccountContext;
import world.trecord.config.security.account.CurrentContext;
import world.trecord.controller.ApiResponse;
import world.trecord.dto.comment.request.CommentCreateRequest;
import world.trecord.dto.comment.request.CommentUpdateRequest;
import world.trecord.dto.comment.response.CommentResponse;
import world.trecord.service.comment.CommentService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}/replies")
    public ApiResponse<Page<CommentResponse>> getReplies(@PathVariable Long commentId,
                                                         @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                         @CurrentContext AccountContext accountContext) {
        return ApiResponse.ok(commentService.getReplies(accountContext.getId(), commentId, pageable));
    }

    @PostMapping
    public ApiResponse<Void> createComment(@RequestBody @Valid CommentCreateRequest request,
                                           @CurrentContext AccountContext accountContext) {
        commentService.createComment(accountContext.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<Void> updateComment(@PathVariable Long commentId,
                                           @RequestBody @Valid CommentUpdateRequest request,
                                           @CurrentContext AccountContext accountContext) {
        commentService.updateComment(accountContext.getId(), commentId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId,
                                           @CurrentContext AccountContext accountContext) {
        commentService.deleteComment(accountContext.getId(), commentId);
        return ApiResponse.ok();
    }
}
