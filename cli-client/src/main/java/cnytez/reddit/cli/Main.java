package cnytez.reddit.cli;

import cnytez.reddit.cli.ui.ConsolePrinter;
import cnytez.reddit.cli.ui.ConsoleReader;

public class Main {

    public static void main(String[] args) {
        ConsoleReader reader = new ConsoleReader();
        ConsolePrinter printer = new ConsolePrinter();

        printer.print("Cum te cheama?");
        String name = reader.readLine();

        printer.println("Salut, " + name + "!");
    }
}
