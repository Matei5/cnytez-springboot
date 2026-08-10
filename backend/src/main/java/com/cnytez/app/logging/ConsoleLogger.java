package com.cnytez.app.logging;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConsoleLogger implements Logger {
    @Override
    public void log(String message, LogLevel level) {
        if (level.equals(LogLevel.ERROR))
            System.err.println(message);
        else
         System.out.println(message);
    }
}