package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Comment;
import cnytez.reddit.app.model.Post;
import cnytez.reddit.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost(Post post);

    long countByPost(Post post);

    List<Comment> findByOwner(User owner);

    // direct post comments
    List<Comment> findByPostAndParentCommentIsNull(Post post);

    // replies to a comment
    List<Comment> findByParentComment(Comment parentComment);

    long countByParentComment(Comment parentComment);
}
