package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_votes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVote {
    
    private Long id;
    private User user;
    private Post post;
    private VoteType voteType;
}
