package cnytez.reddit.cli;

import cnytez.reddit.cli.ui.ConsolePrinter;
import cnytez.reddit.cli.ui.ConsoleReader;
import cnytez.reddit.cli.ui.Menu;
import cnytez.reddit.cli.client.ApiClient;

public class Main {

    public static void main(String[] args) {
        ConsoleReader reader = new ConsoleReader();
        ConsolePrinter printer = new ConsolePrinter();
        ApiClient apiClient = new ApiClient("http://localhost:9090");

        Menu menu = new Menu(reader, printer, apiClient);
        menu.start();
    }
}
