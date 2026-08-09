package com.cnytez.app.repository;

import com.cnytez.app.model.Comment;
import com.cnytez.app.model.CommentVote;
import com.cnytez.app.model.User;
import com.cnytez.app.model.VoteType;
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
