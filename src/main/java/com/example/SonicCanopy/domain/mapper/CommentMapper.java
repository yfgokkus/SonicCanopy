package com.example.SonicCanopy.domain.mapper;

import com.example.SonicCanopy.domain.dto.comment.CommentDto;
import com.example.SonicCanopy.domain.dto.comment.CommentUserInfo;
import com.example.SonicCanopy.domain.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment, CommentUserInfo userInfo, Long eventId) {
        Comment parent = comment.getParent();
        boolean deleted = comment.isDeleted();

        return CommentDto.builder()
                .uuid(comment.getUuid().toString())
                .eventId(eventId)
                .parentUuid(parent != null ? parent.getUuid().toString() : null)
                .deleted(deleted)
                // Only show these if the comment is not deleted
                .content(deleted ? null : comment.getContent())
                .createdAt(deleted ? null : comment.getCreatedAt())
                .likeCount(deleted ? null : comment.getNumberOfLikes())
                .userInfo(deleted ? null : userInfo)
                .interlocutorUsername(deleted || parent == null ? null : parent.getCreatedBy().getUsername())
                .build();
    }
}

