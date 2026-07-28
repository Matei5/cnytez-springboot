package cnytez.reddit.app.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LogManager {
    private final List<Logger> loggers;

    @Autowired
    public LogManager(List<Logger> loggers) {
        this.loggers = new ArrayList<>(loggers);
    }

    public void log(String message) {
        for (Logger logger : loggers) {
            logger.log(message);
        }
    }
}
