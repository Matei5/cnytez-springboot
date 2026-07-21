package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    private Long id;
    private String title;
    private String text;
    private String image;
    private LocalDateTime creationDate;
    private User owner;
    private Post post;
    private Comment parentComment;
    private List<Comment> childComments = new ArrayList<>();
}
