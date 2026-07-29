package cnytez.reddit.cli.ui;

import cnytez.reddit.cli.client.ApiClient;
import cnytez.reddit.cli.dto.LoginRequest;
import cnytez.reddit.cli.dto.RegisterRequest;
import cnytez.reddit.cli.dto.UserDto;
import cnytez.reddit.cli.session.Session;

public class Menu {

    private final ConsoleReader reader;
    private final ConsolePrinter printer;
    private final ApiClient apiClient;
    private final Session session;

    public Menu(
            ConsoleReader reader,
            ConsolePrinter printer,
            ApiClient apiClient,
            Session session
    ) {
        this.reader = reader;
        this.printer = printer;
        this.apiClient = apiClient;
        this.session = session;
    }

    public void start()  {
        boolean running = true;

        while (running) {
            printOptions();
            String option = reader.readLine();

            switch (option) {
                case "1" -> showUsers();
                case "2" -> register();
                case "3" -> login();
                case "4" -> logout();
                case "0" -> running = false;
                default -> printer.println("Invalid option");
            }
        }

        printer.println("Exit");
    }

    private void printOptions() {
        if (session.isLoggedIn()) {
            printer.println("Logged in as: " + session.getCurrentUser().username());
        } else {
            printer.println("Not logged in");
        }

        printer.println("1. Show users");
        printer.println("2. Register");
        printer.println("3. Login");
        printer.println("4. Logout");
        printer.println("0. Exit");
        printer.print("> ");
    }

    private void showUsers() {
        try {
            String json = apiClient.getAllUsers();
            printer.println(json);
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void register() {
        printer.print("Name: ");
        String name = reader.readLine();

        printer.print("Username: ");
        String username = reader.readLine();

        printer.print("Email: ");
        String email = reader.readLine();

        printer.print("Password: ");
        String password = reader.readLine();

        printer.print("Profile photo (optional): ");
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
                    "User created: "
                            + user.username()
                            + " (id=" + user.id() + ")"
            );
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void login() {
        printer.print("Username: ");
        String username = reader.readLine();

        printer.print("Password: ");
        String password = reader.readLine();

        LoginRequest request = new LoginRequest(username, password);

        try {
            UserDto user = apiClient.login(request);
            session.login(user);

            printer.println("Login successful. Welcome, " + user.username() + "!");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }
    private void logout() {
        if (!session.isLoggedIn()) {
            printer.println("No user is logged in.");
            return;
        }

        String username = session.getCurrentUser().username();
        session.logout();

        printer.println("Goodbye, " + username + "!");
    }
}
