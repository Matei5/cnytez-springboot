package com.cnytez.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(columnDefinition = "TEXT")
    private String text;


    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private Instant deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @ToString.Exclude
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @ToString.Exclude
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<Comment> childComments = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // used instanceof to ensure updated instance, not lazy loaded
        if (!(o instanceof Comment)) return false;

        Comment other = (Comment) o;

        // compare only if the id is not null (is in database)
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
