package com.cnytez.app.log;

import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Async;

import java.io.FileWriter;
import java.io.IOException;

@NoArgsConstructor
public class FileLogger implements Logger {
    @Override
    @Async("logExecutor")
    public void log(String message) {
        try (FileWriter writer = new FileWriter("app.log", true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Failed to log message" + e.getMessage());
        }
    }
}