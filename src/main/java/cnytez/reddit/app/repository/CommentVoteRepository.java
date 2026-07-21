package cnytez.reddit.demo.repository;

import cnytez.reddit.demo.model.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

}
