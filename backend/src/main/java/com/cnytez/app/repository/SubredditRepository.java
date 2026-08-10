package com.cnytez.app.repository;

import com.cnytez.app.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, UUID> {

    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);

}
