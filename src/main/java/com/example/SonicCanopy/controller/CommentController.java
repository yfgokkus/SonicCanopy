package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.comment.CommentDto;
import com.example.SonicCanopy.domain.dto.comment.CreateCommentRequest;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.entity.User;
import com.example.SonicCanopy.service.app.CommentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
