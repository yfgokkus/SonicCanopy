package com.example.SonicCanopy.domain.dto.comment;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommentDto(
        String uuid,
        Long eventId,
        String parentUuid,

        String content,
        LocalDateTime createdAt,
        Long likeCount,

        CommentUserInfo userInfo,

        String interlocutorUsername,

        Boolean deleted
) {}
