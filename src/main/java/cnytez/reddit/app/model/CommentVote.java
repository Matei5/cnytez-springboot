package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_votes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVote {

    private User user;
    private Comment comment;
    private VoteType voteType;
}
