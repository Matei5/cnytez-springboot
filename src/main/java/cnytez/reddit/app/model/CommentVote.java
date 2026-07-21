package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "comment_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "comment_id"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;
}