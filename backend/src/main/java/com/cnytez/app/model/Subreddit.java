package com.cnytez.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "subreddits")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subreddit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String displayName;

    @Column(length = 500, nullable = false)
    private String description;

    private String iconUrl;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "subreddit_members",
            joinColumns = @JoinColumn(name = "subreddit_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<User> members = new HashSet<>();

    public void addMember(User user) {
        members.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        // used instanceof to ensure updated instance, not lazy loaded
        if (!(o instanceof Subreddit)) return false;

        Subreddit other = (Subreddit) o;

        // compare only if the id is not null (is in database)
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
