package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.comment.CommentDto;
import com.example.SonicCanopy.domain.dto.comment.CreateCommentRequest;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.service.app.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clubs/{clubId}/events/{eventId}/comments")
@Slf4j
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal User user
    ) {
        CommentDto comment = commentService.createComment(request, user, clubId, eventId);
        return ResponseEntity.ok(ApiResponse.success("Event created successfully", comment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CommentDto>>> getRootComments(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User requester,
            HttpServletRequest request
    ) {
        PagedResponse<CommentDto> response = commentService.getRootComments(
                requester,
                clubId,
                eventId,
                page,
                size,
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{uuid}/replies")
    public ResponseEntity<ApiResponse<PagedResponse<CommentDto>>> getRepliesFlattened(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @PathVariable UUID uuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User requester,
            HttpServletRequest request
    ) {
        PagedResponse<CommentDto> response = commentService.getRepliesFlattened(
                requester,
                uuid,
                clubId,
                eventId,
                page,
                size,
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> softDeleteComment(
            @PathVariable UUID uuid,
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user
    ){
        commentService.softDeleteComment(uuid,clubId,user);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> hardDeleteComment(
            @PathVariable UUID uuid,
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user
    ){
        commentService.hardDeleteComment(uuid, clubId, user);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }

    @PostMapping("/{uuid}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @PathVariable UUID uuid,
            @AuthenticationPrincipal User requester
    ) {
        commentService.likeComment(requester, uuid);
        return ResponseEntity.ok(ApiResponse.success("Comment liked successfully"));
    }

    @PostMapping("/{uuid}/unlike")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable Long clubId,
            @PathVariable Long eventId,
            @PathVariable UUID uuid,
            @AuthenticationPrincipal User requester
    ) {
        commentService.unlikeComment(requester, uuid);
        return ResponseEntity.ok(ApiResponse.success("Comment unliked successfully"));
    }
}
