package com.cnytez.cli;

import com.cnytez.cli.ui.ConsolePrinter;
import com.cnytez.cli.ui.ConsoleReader;
import com.cnytez.cli.ui.Menu;
import com.cnytez.cli.client.ApiClient;
import com.cnytez.cli.session.Session;

public class Main {

    public static void main(String[] args) {
        ConsoleReader reader = new ConsoleReader();
        ConsolePrinter printer = new ConsolePrinter();
        ApiClient apiClient = new ApiClient("http://localhost:9090");
        Session session = new Session();

        Menu menu = new Menu(reader, printer, apiClient, session);
        menu.start();
    }
}
