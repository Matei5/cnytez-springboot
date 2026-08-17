package com.cnytez.app.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RedditMetrics {

    private final Counter registrations;

    public RedditMetrics(MeterRegistry meterRegistry) {
        registrations = Counter.builder("reddit.registrations")
                .description("Number of successful user registrations")
                .register(meterRegistry);
    }

    public void recordRegistration() {
        registrations.increment();
    }
}
