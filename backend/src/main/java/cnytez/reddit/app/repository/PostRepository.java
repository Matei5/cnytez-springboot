package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Post;
import cnytez.reddit.app.model.Subreddit;
import cnytez.reddit.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findBySubreddit(Subreddit subreddit);

    List<Post> findByOwner(User owner);

    List<Post> findBySubredditOrderByCreationDateDesc(Subreddit subreddit);

    List<Post> findAllByOrderByCreationDateDesc();

}
