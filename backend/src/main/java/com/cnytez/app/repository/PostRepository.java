package com.cnytez.app.repository;

import com.cnytez.app.model.Post;
import com.cnytez.app.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findBySubreddit(Subreddit subreddit);

    List<Post> findBySubredditOrderByCreatedAtDesc(Subreddit subreddit);

    List<Post> findAllByOrderByCreatedAtDesc();

    long countBySubreddit(Subreddit subreddit);

}
