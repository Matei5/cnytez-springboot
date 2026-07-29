package cnytez.reddit.cli;

import cnytez.reddit.cli.ui.ConsolePrinter;
import cnytez.reddit.cli.ui.ConsoleReader;
import cnytez.reddit.cli.ui.Menu;

public class Main {

    public static void main(String[] args) {
        ConsoleReader reader = new ConsoleReader();
        ConsolePrinter printer = new ConsolePrinter();

        Menu menu = new Menu(reader, printer);
        menu.start();
    }
}
