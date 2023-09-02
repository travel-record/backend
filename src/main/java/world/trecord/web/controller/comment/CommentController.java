package world.trecord.web.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.web.controller.ApiResponse;
import world.trecord.web.security.CurrentUser;
import world.trecord.web.service.comment.CommentService;
import world.trecord.web.service.comment.request.CommentCreateRequest;
import world.trecord.web.service.comment.request.CommentUpdateRequest;
import world.trecord.web.service.comment.response.CommentResponse;
import world.trecord.web.service.comment.response.CommentUpdateResponse;
import world.trecord.web.service.users.UserContext;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}/replies")
    public ApiResponse<Page<CommentResponse>> getReplies(@PathVariable("commentId") Long commentId,
                                                         @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                         @CurrentUser UserContext userContext) {
        Long viewerId = (userContext != null) ? userContext.getId() : null;
        return ApiResponse.ok(commentService.getReplies(commentId, viewerId, pageable));
    }

    @PostMapping
    public ApiResponse<Void> createComment(@RequestBody @Valid CommentCreateRequest request, @CurrentUser UserContext userContext) {
        commentService.createComment(userContext.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentUpdateResponse> updateComment(@PathVariable("commentId") Long commentId,
                                                            @RequestBody @Valid CommentUpdateRequest request,
                                                            @CurrentUser UserContext userContext) {
        return ApiResponse.ok(commentService.updateComment(userContext.getId(), commentId, request));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable("commentId") Long commentId, @CurrentUser UserContext userContext) {
        commentService.deleteComment(userContext.getId(), commentId);
        return ApiResponse.ok();
    }
}
