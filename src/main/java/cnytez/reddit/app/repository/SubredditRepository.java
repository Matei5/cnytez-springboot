package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Post;
import cnytez.reddit.app.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Long> {

    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);

    List<Post> findAllByOrderByCreationDateDesc();
}
