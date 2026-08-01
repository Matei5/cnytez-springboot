package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Comment;
import cnytez.reddit.app.model.Post;
import cnytez.reddit.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPost(Post post);

    long countByPost(Post post);

    List<Comment> findByOwner(User owner);

    // direct post comments
    List<Comment> findByPostAndParentCommentIsNull(Post post);

    // replies to a comment
    List<Comment> findByParentComment(Comment parentComment);

    long countByParentComment(Comment parentComment);
}
