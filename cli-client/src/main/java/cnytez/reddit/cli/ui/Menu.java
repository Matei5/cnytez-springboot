package cnytez.reddit.cli.ui;

import cnytez.reddit.cli.client.ApiClient;

import cnytez.reddit.cli.dto.RegisterRequest;
import cnytez.reddit.cli.dto.UserDto;

public class Menu {

    private final ConsoleReader reader;
    private final ConsolePrinter printer;
    private final ApiClient apiClient;

    public Menu(
            ConsoleReader reader,
            ConsolePrinter printer,
            ApiClient apiClient
    ) {
        this.reader = reader;
        this.printer = printer;
        this.apiClient = apiClient;
    }

    public void start()  {
        boolean running = true;

        while (running) {
            printOptions();
            String option = reader.readLine();

            switch (option) {
                case "1" -> showUsers();
                case "2" -> register();
                case "0" -> running = false;
                default -> printer.println("Invalid option");
            }
        }

        printer.println("Exit");
    }

    private void printOptions() {
        printer.println("");
        printer.println("1. Afiseaza utilizatorii");
        printer.println("2. Inregistrare");
        printer.println("0. Iesire");
        printer.print("> ");
    }

    private void showUsers() {
        try {
            String json = apiClient.getAllUsers();
            printer.println(json);
        } catch (IllegalStateException exception) {
            printer.println("Eroare: " + exception.getMessage());
        }
    }

    private void register() {
        printer.print("Nume: ");
        String name = reader.readLine();

        printer.print("Username: ");
        String username = reader.readLine();

        printer.print("Email: ");
        String email = reader.readLine();

        printer.print("Parola: ");
        String password = reader.readLine();

        printer.print("Poza de profil (optional): ");
        String profilePhoto = reader.readLine();

        if (profilePhoto.isBlank()) {
            profilePhoto = null;
        }

        RegisterRequest request = new RegisterRequest(
                name,
                username,
                email,
                password,
                profilePhoto
        );

        try {
            UserDto user = apiClient.register(request);
            printer.println(
                    "Utilizator creat: "
                            + user.username()
                            + " (id=" + user.id() + ")"
            );
        } catch (IllegalStateException exception) {
            printer.println("Eroare: " + exception.getMessage());
        }
    }

}
