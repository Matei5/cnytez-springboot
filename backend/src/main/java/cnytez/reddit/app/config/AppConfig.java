package cnytez.reddit.app.config;

import cnytez.reddit.app.log.ConsoleLogger;
import cnytez.reddit.app.log.FileLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
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
}
