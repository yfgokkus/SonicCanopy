package com.example.SonicCanopy.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_action_logs")
public class CommentActionLog {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    private CommentActionType actionType;

    @ManyToOne(optional = false)
    private Comment comment;

    private LocalDateTime performedAt;

    private ClubRole actorRole;
}