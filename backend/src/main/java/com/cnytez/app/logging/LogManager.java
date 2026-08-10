package com.cnytez.app.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class LogManager {
    private final List<Logger> loggers;

    @Autowired
    public LogManager(List<Logger> loggers) {
        this.loggers = new ArrayList<>(loggers);
    }

    public void log(String message, LogLevel level) {
        LocalDateTime timeStamp = LocalDateTime.now();
        for (Logger logger : loggers) {
            logger.log(("[" + timeStamp + "] " + level + " " + message), level);
        }
    }
}
