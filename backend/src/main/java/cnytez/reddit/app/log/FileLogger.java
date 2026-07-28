package cnytez.reddit.app.log;

import lombok.NoArgsConstructor;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@NoArgsConstructor
public class FileLogger implements Logger {
    @Override
    public void log(String message) {
        LocalDateTime timeStamp = LocalDateTime.now();
        try {
            FileWriter writer = new FileWriter("app.log", true);
            writer.write("[" + timeStamp + "] " + message + "\n");
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to log message" + e.getMessage());
        }
    }
}