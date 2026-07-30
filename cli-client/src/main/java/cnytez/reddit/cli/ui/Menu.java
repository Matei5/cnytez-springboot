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

    public void start() {
        boolean running = true;

        while (running) {
            printMainMenu();
            String option = reader.readLine();

            switch (option) {
                case "1" -> accountMenu();
                case "2" -> subredditMenu();
                case "3" -> postMenu();
                case "4" -> commentMenu();
                case "0" -> running = false;
                default -> printer.println("Invalid option.");
            }
        }

        printer.println("Goodbye!");
    }

    private void printMainMenu() {
        printer.println("");

        if (session.isLoggedIn()) {
            printer.println("Logged in as: " + session.getCurrentUser().username());
        } else {
            printer.println("Not logged in");
        }

        printer.println("1. Account");
        printer.println("2. Subreddits");
        printer.println("3. Posts");
        printer.println("4. Comments");
        printer.println("0. Exit");
        printer.print("> ");
    }

    private void accountMenu() {
        boolean open = true;

        while (open) {
            printer.println("");
            printer.println("--- Account ---");

            if (session.isLoggedIn()) {
                printer.println("Logged in as: " + session.getCurrentUser().username());
                printer.println("1. Logout");
            } else {
                printer.println("1. Register");
                printer.println("2. Login");
            }

            printer.println("0. Back");
            printer.print("> ");

            String option = reader.readLine();

            if (session.isLoggedIn()) {
                switch (option) {
                    case "1" -> logout();
                    case "0" -> open = false;
                    default -> printer.println("Invalid option.");
                }
            } else {
                switch (option) {
                    case "1" -> register();
                    case "2" -> login();
                    case "0" -> open = false;
                    default -> printer.println("Invalid option.");
                }
            }
        }
    }

    private void subredditMenu() {
        boolean open = true;

        while (open) {
            printer.println("");
            printer.println("--- Subreddits ---");
            printer.println("1. Show subreddits");

            if (session.isLoggedIn()) {
                printer.println("2. Create subreddit");
                printer.println("3. Join subreddit");
            }

            printer.println("0. Back");
            printer.print("> ");

            switch (reader.readLine()) {
                case "1" -> showSubreddits();
                case "2" -> createSubreddit();
                case "3" -> joinSubreddit();
                case "0" -> open = false;
                default -> printer.println("Invalid option.");
            }
        }
    }

    private void postMenu() {
        boolean open = true;

        while (open) {
            printer.println("");
            printer.println("--- Posts ---");
            printer.println("1. Show posts");
            printer.println("2. Show posts by subreddit");
            printer.println("3. View post");

            if (session.isLoggedIn()) {
                printer.println("4. Create post");
                printer.println("5. Vote post");
                printer.println("6. Delete post");
            }

            printer.println("0. Back");
            printer.print("> ");

            switch (reader.readLine()) {
                case "1" -> showPosts();
                case "2" -> showPostsBySubreddit();
                case "3" -> viewPost();
                case "4" -> createPost();
                case "5" -> votePost();
                case "6" -> deletePost();
                case "0" -> open = false;
                default -> printer.println("Invalid option.");
            }
        }
    }

    private void commentMenu() {
        boolean open = true;

        while (open) {
            printer.println("");
            printer.println("--- Comments ---");
            printer.println("1. Show comments for post");
            printer.println("2. Show replies");

            if (session.isLoggedIn()) {
                printer.println("3. Create comment");
                printer.println("4. Reply to comment");
                printer.println("5. Vote comment");
                printer.println("6. Delete comment");
            }

            printer.println("0. Back");
            printer.print("> ");

            switch (reader.readLine()) {
                case "1" -> showCommentsForPost();
                case "2" -> showReplies();
                case "3" -> createComment();
                case "4" -> replyToComment();
                case "5" -> voteComment();
                case "6" -> deleteComment();
                case "0" -> open = false;
                default -> printer.println("Invalid option.");
            }
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
            printPosts(posts);
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void showPostsBySubreddit() {
        printer.print("Subreddit ID: ");

        try {
            Long subredditId = Long.parseLong(reader.readLine());
            List<PostDto> posts = apiClient.getPostsBySubreddit(subredditId);
            printPosts(posts);
        } catch (NumberFormatException exception) {
            printer.println("Invalid subreddit ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }

    private void printPosts(List<PostDto> posts) {
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
                            + " | comments: "
                            + post.commentCount()
            );
        }
    }

    private void viewPost() {
        printer.print("Post ID: ");

        try {
            Long postId = Long.parseLong(reader.readLine());
            PostDto post = apiClient.getPostById(postId);

            String image = post.image();
            if (image == null || image.isBlank()) {
                image = "none";
            }

            printer.println("");
            printer.println("--- Post #" + post.id() + " ---");
            printer.println("r/" + post.subredditName());
            printer.println(post.title());
            printer.println("");
            printer.println(post.text());
            printer.println("");
            printer.println("Posted by: " + post.ownerUsername());
            printer.println("Created at: " + post.createdAt());
            printer.println(
                    "Score: "
                            + post.score()
                            + " ("
                            + post.upvotes()
                            + " up, "
                            + post.downvotes()
                            + " down)"
            );
            printer.println("Comments: " + post.commentCount());
            printer.println("Image: " + image);
        } catch (NumberFormatException exception) {
            printer.println("Invalid post ID.");
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
                                + " | replies: "
                                + comment.replyCount()
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
                                + " | replies: "
                                + reply.replyCount()
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

    private void deletePost() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Post ID: ");

        try {
            Long postId = Long.parseLong(reader.readLine());
            Long userId = session.getCurrentUser().id();

            apiClient.deletePost(postId, userId);
            printer.println("Post deleted.");
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

    private void deleteComment() {
        if (!session.isLoggedIn()) {
            printer.println("You must log in first.");
            return;
        }

        printer.print("Comment ID: ");

        try {
            Long commentId = Long.parseLong(reader.readLine());
            Long userId = session.getCurrentUser().id();

            apiClient.deleteComment(commentId, userId);
            printer.println("Comment deleted.");
        } catch (NumberFormatException exception) {
            printer.println("Invalid comment ID.");
        } catch (IllegalStateException exception) {
            printer.println("Error: " + exception.getMessage());
        }
    }
}
