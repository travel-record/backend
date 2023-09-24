package world.trecord.controller.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import world.trecord.config.security.AccountContext;
import world.trecord.config.security.CurrentContext;
import world.trecord.config.security.UserContext;
import world.trecord.controller.ApiResponse;
import world.trecord.dto.comment.request.CommentCreateRequest;
import world.trecord.dto.comment.request.CommentUpdateRequest;
import world.trecord.dto.comment.response.CommentResponse;
import world.trecord.service.comment.CommentService;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}/replies")
    public ApiResponse<Page<CommentResponse>> getReplies(@PathVariable Long commentId,
                                                         @PageableDefault(sort = "createdDateTime", direction = Sort.Direction.ASC) Pageable pageable,
                                                         @CurrentContext AccountContext accountContext) {
        Optional<Long> itOpt = Optional.ofNullable(accountContext.getId());
        return ApiResponse.ok(commentService.getReplies(itOpt, commentId, pageable));
    }

    @PostMapping
    public ApiResponse<Void> createComment(@RequestBody @Valid CommentCreateRequest request,
                                           @CurrentContext UserContext userContext) {
        commentService.createComment(userContext.getId(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<Void> updateComment(@PathVariable Long commentId,
                                           @RequestBody @Valid CommentUpdateRequest request,
                                           @CurrentContext UserContext userContext) {
        commentService.updateComment(userContext.getId(), commentId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId,
                                           @CurrentContext UserContext userContext) {
        commentService.deleteComment(userContext.getId(), commentId);
        return ApiResponse.ok();
    }
}
