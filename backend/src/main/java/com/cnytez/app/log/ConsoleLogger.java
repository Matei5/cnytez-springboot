package com.cnytez.app.log;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println(message);
    }
}