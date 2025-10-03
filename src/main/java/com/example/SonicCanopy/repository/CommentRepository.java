package com.example.SonicCanopy.repository;

import com.example.SonicCanopy.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
            "WHERE c.path LIKE CONCAT(:rootPath, '%') AND c.id <> :rootId " +
            "ORDER BY c.createdAt ASC")
    Page<Comment> findRepliesFlattened(@Param("rootPath") String rootPath,
                                       @Param("rootId") Long rootId,
                                       Pageable pageable);

    Page<Comment> findByEventIdAndParentIsNull(Long eventId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parent.id = :commentId")
    long replyCount(@Param("commentId") Long commentId);

    Optional<Comment> findByUuid(UUID uuid);
}
