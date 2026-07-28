package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Post;
import cnytez.reddit.app.model.PostVote;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    Optional<PostVote> findByUserAndPost(User user, Post post);

    long countByPostAndVoteType(Post post, VoteType voteType);

    void deleteByUserAndPost(User user, Post post);
}
