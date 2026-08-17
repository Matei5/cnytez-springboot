package com.cnytez.app.monitoring;

import com.cnytez.app.repository.CommentRepository;
import com.cnytez.app.repository.CommentVoteRepository;
import com.cnytez.app.repository.PostRepository;
import com.cnytez.app.repository.PostVoteRepository;
import com.cnytez.app.repository.SubredditRepository;
import com.cnytez.app.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class GlobalStatisticsMetrics {

    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;

    private final AtomicLong users = new AtomicLong();
    private final AtomicLong communities = new AtomicLong();
    private final AtomicLong posts = new AtomicLong();
    private final AtomicLong comments = new AtomicLong();
    private final AtomicLong votes = new AtomicLong();

    public GlobalStatisticsMetrics(
            MeterRegistry meterRegistry,
            UserRepository userRepository,
            SubredditRepository subredditRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            PostVoteRepository postVoteRepository,
            CommentVoteRepository commentVoteRepository) {
        this.userRepository = userRepository;
        this.subredditRepository = subredditRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.postVoteRepository = postVoteRepository;
        this.commentVoteRepository = commentVoteRepository;

        registerGauge(meterRegistry, "reddit.users.current", "Current number of users", users);
        registerGauge(meterRegistry, "reddit.communities.current", "Current number of communities", communities);
        registerGauge(meterRegistry, "reddit.posts.current", "Current number of posts", posts);
        registerGauge(meterRegistry, "reddit.comments.current", "Current number of comments", comments);
        registerGauge(meterRegistry, "reddit.votes.current", "Current number of post and comment votes", votes);
    }

    private void registerGauge(MeterRegistry meterRegistry, String name, String description, AtomicLong value) {
        Gauge.builder(name, value, AtomicLong::get)
                .description(description)
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${monitoring.global-statistics.refresh-interval-ms:60000}")
    @Transactional(readOnly = true)
    public void refresh() {
        long currentUsers = userRepository.count();
        long currentCommunities = subredditRepository.count();
        long currentPosts = postRepository.count();
        long currentComments = commentRepository.count();
        long currentVotes = postVoteRepository.count() + commentVoteRepository.count();

        users.set(currentUsers);
        communities.set(currentCommunities);
        posts.set(currentPosts);
        comments.set(currentComments);
        votes.set(currentVotes);
    }
}
