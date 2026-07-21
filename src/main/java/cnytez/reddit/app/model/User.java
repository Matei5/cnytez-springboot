package cnytez.reddit.app.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String profilePhoto;
}
