package com.example.SonicCanopy.domain.dto.comment;

import lombok.Builder;

@Builder
public record CommentUserInfo (
        String username,
        String userHref
        //TODO: String profileImageUrl
) {}
