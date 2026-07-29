package cnytez.reddit.cli.ui;

import cnytez.reddit.cli.client.ApiClient;
import cnytez.reddit.cli.dto.CommentDto;
import cnytez.reddit.cli.dto.CreateCommentRequest;
import cnytez.reddit.cli.dto.CreatePostRequest;
import cnytez.reddit.cli.dto.CreateSubredditRequest;
import cnytez.reddit.cli.dto.LoginRequest;
import cnytez.reddit.cli.dto.PostDto;
import cnytez.reddit.cli.dto.RegisterRequest;
import cnytez.reddit.cli.dto.SubredditDto;
import cnytez.reddit.cli.dto.UserDto;
import cnytez.reddit.cli.dto.VoteRequest;
import cnytez.reddit.cli.dto.VoteType;
import cnytez.reddit.cli.session.Session;

import java.util.List;

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
                case "5" -> showSubreddits();
                case "6" -> createSubreddit();
                case "7" -> joinSubreddit();
                case "8" -> showPosts();
                case "9" -> createPost();
                case "10" -> showCommentsForPost();
                case "11" -> createComment();
                case "12" -> showReplies();
                case "13" -> replyToComment();
                case "14" -> votePost();
                case "15" -> voteComment();
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
        printer.println("5. Show subreddits");
        printer.println("6. Create subreddit");
        printer.println("7. Join subreddit");
        printer.println("8. Show posts");
        printer.println("9. Create post");
        printer.println("10. Show comments for post");
        printer.println("11. Create comment");
        printer.println("12. Show replies");
        printer.println("13. Reply to comment");
        printer.println("14. Vote post");
        printer.println("15. Vote comment");
        printer.println("0. Exit");
        printer.print("> ");
    }

    private void showUsers() {
        try {
            List<UserDto> users = apiClient.getAllUsers();

            if (users.isEmpty()) {
                printer.println("No users found.");
                return;
            }

            for (UserDto user : users) {
                printer.println(
                        user.id()
                                + " | "
                                + user.username()
                                + " | "
                                + user.email()
                );
            }
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

    private void showSubreddits() {
        try {
            List<SubredditDto> subreddits = apiClient.getAllSubreddits();

            if (subreddits.isEmpty()) {
                printer.println("No subreddits found.");
                return;
            }

            for (SubredditDto subreddit : subreddits) {
                printer.println(
                        subreddit.id()
                                + " | r/"
                                + subreddit.name()
                                + " | owner: "
                                + subreddit.ownerUsername()
                                + " | members: "
                                + subreddit.memberCount()
                );
            }
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void createSubreddit() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Name: ");
        String name = reader.readLine();

        printer.print("Photo (optional): ");
        String photo = reader.readLine();

        printer.print("Banner (optional): ");
        String banner = reader.readLine();

        if (photo.isBlank()) {
            photo = null;
        }
        if (banner.isBlank()) {
            banner = null;
        }

        CreateSubredditRequest request = new CreateSubredditRequest(
                name,
                photo,
                banner,
                session.getCurrentUser().id()
        );

        try {
            SubredditDto subreddit = apiClient.createSubreddit(request);
            printer.println(
                    "Subreddit created: r/"
                            + subreddit.name()
                            + " (id="
                            + subreddit.id()
                            + ")"
            );
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void joinSubreddit() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Subreddit ID: ");

        try {
            Long subredditId = Long.parseLong(reader.readLine());
            Long userId = session.getCurrentUser().id();

            SubredditDto subreddit = apiClient.joinSubreddit(
                    subredditId,
                    userId
            );

            printer.println(
                    "Joined r/"
                            + subreddit.name()
                            + ". Members: "
                            + subreddit.memberCount()
            );
        } catch (NumberFormatException exception) {
            printer.println("Invalid subreddit ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void showPosts() {
        try {
            List<PostDto> posts = apiClient.getAllPosts();

            if (posts.isEmpty()) {
                printer.println("No posts found.");
                return;
            }

            for (PostDto post : posts) {
                printer.println(
                        post.id()
                                + " | r/"
                                + post.subredditName()
                                + " | "
                                + post.title()
                                + " | by "
                                + post.ownerUsername()
                                + " | score: "
                                + post.score()
                );
            }
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void createPost() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Title: ");
        String title = reader.readLine();

        printer.print("Text: ");
        String text = reader.readLine();

        printer.print("Image (optional): ");
        String image = reader.readLine();

        printer.print("Subreddit ID: ");

        try {
            Long subredditId = Long.parseLong(reader.readLine());

            if (image.isBlank()) {
                image = null;
            }

            CreatePostRequest request = new CreatePostRequest(
                    title,
                    text,
                    image,
                    subredditId,
                    session.getCurrentUser().id()
            );

            PostDto post = apiClient.createPost(request);
            printer.println(
                    "Post created: "
                            + post.title()
                            + " (id="
                            + post.id()
                            + ")"
            );
        } catch (NumberFormatException exception) {
            printer.println("Invalid subreddit ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void showCommentsForPost() {
        printer.print("Post ID: ");

        try {
            Long postId = Long.parseLong(reader.readLine());
            List<CommentDto> comments = apiClient.getCommentsByPost(postId);

            if (comments.isEmpty()) {
                printer.println("No comments found.");
                return;
            }

            for (CommentDto comment : comments) {
                printer.println(
                        comment.id()
                                + " | by "
                                + comment.ownerUsername()
                                + " | "
                                + comment.text()
                                + " | score: "
                                + comment.score()
                );
            }
        } catch (NumberFormatException exception) {
            printer.println("Invalid post ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void createComment() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Title: ");
        String title = reader.readLine();

        printer.print("Text: ");
        String text = reader.readLine();

        printer.print("Image (optional): ");
        String image = reader.readLine();

        printer.print("Post ID: ");

        try {
            Long postId = Long.parseLong(reader.readLine());

            if (image.isBlank()) {
                image = null;
            }

            CreateCommentRequest request = new CreateCommentRequest(
                    title,
                    text,
                    image,
                    postId,
                    session.getCurrentUser().id(),
                    null
            );

            CommentDto comment = apiClient.createComment(request);
            printer.println(
                    "Comment created (id="
                            + comment.id()
                            + ", score="
                            + comment.score()
                            + ")"
            );
        } catch (NumberFormatException exception) {
            printer.println("Invalid post ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void showReplies() {
        printer.print("Comment ID: ");

        try {
            Long commentId = Long.parseLong(reader.readLine());
            List<CommentDto> replies = apiClient.getReplies(commentId);

            if (replies.isEmpty()) {
                printer.println("No replies found.");
                return;
            }

            for (CommentDto reply : replies) {
                printer.println(
                        reply.id()
                                + " | by "
                                + reply.ownerUsername()
                                + " | "
                                + reply.text()
                                + " | score: "
                                + reply.score()
                );
            }
        } catch (NumberFormatException exception) {
            printer.println("Invalid comment ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void replyToComment() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Title: ");
        String title = reader.readLine();

        printer.print("Text: ");
        String text = reader.readLine();

        printer.print("Image (optional): ");
        String image = reader.readLine();

        try {
            printer.print("Post ID: ");
            Long postId = Long.parseLong(reader.readLine());

            printer.print("Parent comment ID: ");
            Long parentCommentId = Long.parseLong(reader.readLine());

            if (image.isBlank()) {
                image = null;
            }

            CreateCommentRequest request = new CreateCommentRequest(
                    title,
                    text,
                    image,
                    postId,
                    session.getCurrentUser().id(),
                    parentCommentId
            );

            CommentDto reply = apiClient.createComment(request);
            printer.println(
                    "Reply created (id="
                            + reply.id()
                            + ", score="
                            + reply.score()
                            + ")"
            );
        } catch (NumberFormatException exception) {
            printer.println("Post ID and comment ID must be numbers.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void votePost() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        try {
            printer.print("Post ID: ");
            Long postId = Long.parseLong(reader.readLine());

            printer.print("Vote (U/D): ");
            String choice = reader.readLine().toUpperCase();

            VoteType voteType;
            if (choice.equals("U")) {
                voteType = VoteType.UPVOTE;
            } else if (choice.equals("D")) {
                voteType = VoteType.DOWNVOTE;
            } else {
                printer.println("Invalid vote. Enter U or D.");
                return;
            }

            VoteRequest request = new VoteRequest(
                    session.getCurrentUser().id(),
                    voteType
            );

            PostDto post = apiClient.votePost(postId, request);
            printer.println(
                    "Post score: "
                            + post.score()
                            + " ("
                            + post.upvotes()
                            + " up, "
                            + post.downvotes()
                            + " down)"
            );
        } catch (NumberFormatException exception) {
            printer.println("Invalid post ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void voteComment() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        try {
            printer.print("Comment ID: ");
            Long commentId = Long.parseLong(reader.readLine());

            printer.print("Vote (U/D): ");
            String choice = reader.readLine().toUpperCase();

            VoteType voteType;
            if (choice.equals("U")) {
                voteType = VoteType.UPVOTE;
            } else if (choice.equals("D")) {
                voteType = VoteType.DOWNVOTE;
            } else {
                printer.println("Invalid vote. Enter U or D.");
                return;
            }

            VoteRequest request = new VoteRequest(
                    session.getCurrentUser().id(),
                    voteType
            );

            CommentDto comment = apiClient.voteComment(commentId, request);
            printer.println(
                    "Comment score: "
                            + comment.score()
                            + " ("
                            + comment.upvotes()
                            + " up, "
                            + comment.downvotes()
                            + " down)"
            );
        } catch (NumberFormatException exception) {
            printer.println("Invalid comment ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }
}
