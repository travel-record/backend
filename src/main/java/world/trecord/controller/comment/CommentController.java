package world.trecord.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.CurrentUser;
import world.trecord.controller.ApiResponse;
import world.trecord.service.comment.CommentService;
import world.trecord.service.comment.request.CommentCreateRequest;
import world.trecord.service.comment.request.CommentUpdateRequest;
import world.trecord.service.comment.response.CommentResponse;
import world.trecord.service.comment.response.CommentUpdateResponse;
import world.trecord.service.users.UserContext;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}/replies")
    public ApiResponse<Page<CommentResponse>> getReplies(@PathVariable("commentId") Long commentId,
                                                         @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                         @CurrentUser UserContext userContext) {
        Optional<Long> viewerId = Optional.ofNullable(userContext).map(UserContext::getId);
        return ApiResponse.ok(commentService.getReplies(viewerId, commentId, pageable));
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
