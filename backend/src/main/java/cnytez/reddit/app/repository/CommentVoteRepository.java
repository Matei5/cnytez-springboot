package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Comment;
import cnytez.reddit.app.model.CommentVote;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, UUID> {

    Optional<CommentVote> findByUserAndComment(User user, Comment comment);

    long countByCommentAndVoteType(Comment comment, VoteType voteType);

    void deleteByUserAndComment(User user, Comment comment);
}
