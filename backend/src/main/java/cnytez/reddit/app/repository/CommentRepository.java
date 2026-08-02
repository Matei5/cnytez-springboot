package cnytez.reddit.app.repository;

import cnytez.reddit.app.model.Comment;
import cnytez.reddit.app.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {


    long countByPost(Post post);

    // direct post comments
    List<Comment> findByPostAndParentCommentIsNull(Post post);

    // replies to a comment
    List<Comment> findByParentComment(Comment parentComment);

    long countByPost_Id(UUID postId);
}
