package com.cnytez.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
    @ToString.Exclude
    private Filter filter;

    @Column(nullable = false)
    private Instant creationDate;

    private Instant updatedAt;

    private Instant deletionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id", nullable = false)
    @ToString.Exclude
    private Subreddit subreddit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // used instanceof to ensure updated instance, not lazy loaded
        if (!(o instanceof Post)) return false;

        Post other = (Post) o;

        // compare only if the id is not null (is in database)
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
