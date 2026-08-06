package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "posts")
@Data
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

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Filter filter;

    @Column(nullable = false)
    private Instant creationDate;

    private Instant updatedAt;

    private Instant deletionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Subreddit subreddit;
}
