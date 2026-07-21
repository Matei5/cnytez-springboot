package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    private Long id;
    private String title;
    private String text;
    private String image;
    private LocalDateTime creationDate;
    private User owner;
    private Subreddit subreddit;
}
