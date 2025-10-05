package com.example.SonicCanopy.repository;

import com.example.SonicCanopy.domain.entity.Comment;
import com.example.SonicCanopy.domain.entity.CommentLike;
import com.example.SonicCanopy.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndUser(Comment comment, User user);
    Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
    long countByComment(Comment comment);
    void deleteByCommentAndUser(Comment comment, User user);
}
