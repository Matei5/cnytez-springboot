package cnytez.reddit.app.config;

import cnytez.reddit.app.log.ConsoleLogger;
import cnytez.reddit.app.log.FileLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
