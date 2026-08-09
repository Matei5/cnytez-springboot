package com.cnytez.app.repository;

import com.cnytez.app.model.Post;
import com.cnytez.app.model.PostVote;
import com.cnytez.app.model.User;
import com.cnytez.app.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, UUID> {

    Optional<PostVote> findByUserAndPost(User user, Post post);

    long countByPostAndVoteType(Post post, VoteType voteType);

    void deleteByUserAndPost(User user, Post post);
}
