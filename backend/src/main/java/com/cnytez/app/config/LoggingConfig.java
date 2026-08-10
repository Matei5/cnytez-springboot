package com.cnytez.app.config;

import com.cnytez.app.logging.ConsoleLogger;
import com.cnytez.app.logging.FileLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class LoggingConfig {
    @Bean
    public ConsoleLogger consoleLogger() {
        return new ConsoleLogger();
    }

    @Bean
    public FileLogger fileLogger() {
        return new FileLogger();
    }

    @Bean
    ThreadPoolTaskExecutor logExecutor() {
        ThreadPoolTaskExecutor logExecutor = new ThreadPoolTaskExecutor();
        logExecutor.setCorePoolSize(1);
        logExecutor.setMaxPoolSize(1);
        logExecutor.setQueueCapacity(1000);
        logExecutor.initialize();

        return logExecutor;
    }
}
