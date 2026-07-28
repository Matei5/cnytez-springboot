package cnytez.reddit.app.log;

import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        LocalDateTime timeStamp = LocalDateTime.now();
        System.out.println("[" + timeStamp + "] " + message);
    }
}