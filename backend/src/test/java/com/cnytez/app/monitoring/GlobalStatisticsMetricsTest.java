package com.cnytez.app.monitoring;

import com.cnytez.app.repository.CommentRepository;
import com.cnytez.app.repository.CommentVoteRepository;
import com.cnytez.app.repository.PostRepository;
import com.cnytez.app.repository.PostVoteRepository;
import com.cnytez.app.repository.SubredditRepository;
import com.cnytez.app.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalStatisticsMetricsTest {

    @Test
    void refreshUpdatesAllGlobalStatistics() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UserRepository userRepository = mock(UserRepository.class);
        SubredditRepository subredditRepository = mock(SubredditRepository.class);
        PostRepository postRepository = mock(PostRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        PostVoteRepository postVoteRepository = mock(PostVoteRepository.class);
        CommentVoteRepository commentVoteRepository = mock(CommentVoteRepository.class);

        when(userRepository.count()).thenReturn(12L);
        when(subredditRepository.count()).thenReturn(4L);
        when(postRepository.count()).thenReturn(25L);
        when(commentRepository.count()).thenReturn(80L);
        when(postVoteRepository.count()).thenReturn(100L);
        when(commentVoteRepository.count()).thenReturn(40L);

        GlobalStatisticsMetrics metrics = new GlobalStatisticsMetrics(
                meterRegistry,
                userRepository,
                subredditRepository,
                postRepository,
                commentRepository,
                postVoteRepository,
                commentVoteRepository);

        metrics.refresh();

        assertThat(gaugeValue(meterRegistry, "reddit.users.current")).isEqualTo(12);
        assertThat(gaugeValue(meterRegistry, "reddit.communities.current")).isEqualTo(4);
        assertThat(gaugeValue(meterRegistry, "reddit.posts.current")).isEqualTo(25);
        assertThat(gaugeValue(meterRegistry, "reddit.comments.current")).isEqualTo(80);
        assertThat(gaugeValue(meterRegistry, "reddit.votes.current")).isEqualTo(140);
    }

    private double gaugeValue(SimpleMeterRegistry meterRegistry, String name) {
        return meterRegistry.get(name).gauge().value();
    }
}
