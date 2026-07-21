package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subreddits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subreddit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String photo;
    private String banner;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "subreddit_members",
            joinColumns = @JoinColumn(name = "subreddit_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> members = new HashSet<>();

    public void addMember(User user) {
        members.add(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }
}
