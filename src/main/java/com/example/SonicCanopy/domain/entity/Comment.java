package com.example.SonicCanopy.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comments",
        indexes = {
                @Index(name = "idx_comment_event", columnList = "event_id"),
                @Index(name = "idx_comment_parent", columnList = "parent_id"),
                @Index(name = "idx_comment_likes_createdAt", columnList = "number_of_likes, created_at")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Long numberOfLikes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true) //default LAZY
    private List<Comment> replies;

    @Column(nullable = false)
    private String path;

    private boolean deleted = false;

    public String getContent() {
        return deleted ? "[deleted]" : content;
    }

    public User getCreatedBy() {
        return deleted ? null : createdBy;
    }

    public LocalDateTime getCreatedAt() {return deleted? null : createdAt;}

    public Long getNumberOfLikes() {return deleted ? null : numberOfLikes;}

    public  List<Comment> getReplies() {return deleted ? null : replies;}

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }

        if (this.path == null) {
            if (this.parent == null) {
                this.path = "/" + this.uuid + "/";
            } else {
                this.path = this.parent.getPath() + this.uuid + "/";
            }
        }
    }

}