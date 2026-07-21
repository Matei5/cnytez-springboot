package cnytez.reddit.demo.model;

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

    private Long id;
    private String name;
    private String photo;
    private String banner;
    private LocalDateTime creationDate;
    private User owner;
    private Set<User> members = new HashSet<>();

}
