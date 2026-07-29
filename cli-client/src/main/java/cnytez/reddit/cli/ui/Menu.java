package cnytez.reddit.cli.ui;

import cnytez.reddit.cli.ui.*;

public class Menu {

    private final ConsoleReader reader;
    private final ConsolePrinter printer;

    public Menu(ConsoleReader reader, ConsolePrinter printer) {
        this.reader = reader;
        this.printer = printer;
    }

    public void start()  {
        boolean running = true;

        while (running) {
            printOptions();
            String option = reader.readLine();

            switch (option) {
                case "1" -> printer.print("Clientul functioneaza");
                case "0" -> running = false;
                default -> printer.println("Invalid option");
            }
        }

        printer.println("Exit");
    }

    private void printOptions() {
        printer.println("");
        printer.println("1. Test");
        printer.println("0. Iesire");
        printer.print("> ");
    }



}
