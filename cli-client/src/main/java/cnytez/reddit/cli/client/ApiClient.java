package cnytez.reddit.cli.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<UserDto> getAllUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);
            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<UserDto>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to connect to the backend", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The HTTP request was interrupted", exception);
        }
    }

    public UserDto register(RegisterRequest registerRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(registerRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), UserDto.class);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public UserDto login(LoginRequest loginRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(loginRequest);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), UserDto.class);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public List<SubredditDto> getAllSubreddits() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/subreddits"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<SubredditDto>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public SubredditDto createSubreddit(CreateSubredditRequest createRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(createRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/subreddits"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), SubredditDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public SubredditDto joinSubreddit(Long subredditId, Long userId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl
                                + "/api/subreddits/"
                                + subredditId
                                + "/join?userId="
                                + userId
                ))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), SubredditDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public List<PostDto> getAllPosts() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts"))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<PostDto>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public PostDto getPostById(Long postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/posts/" + postId))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), PostDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public PostDto createPost(CreatePostRequest createRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(createRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/posts"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), PostDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public List<CommentDto> getCommentsByPost(Long postId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl + "/api/comments/by-post/" + postId
                ))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<CommentDto>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public CommentDto createComment(CreateCommentRequest createRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(createRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/comments"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), CommentDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public List<CommentDto> getReplies(Long commentId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl + "/api/comments/" + commentId + "/replies"
                ))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<CommentDto>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public PostDto votePost(Long postId, VoteRequest voteRequest) {
        try {
            String jsonBody = objectMapper.writeValueAsString(voteRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            baseUrl + "/api/posts/" + postId + "/vote"
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), PostDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }

    public CommentDto voteComment(
            Long commentId,
            VoteRequest voteRequest
    ) {
        try {
            String jsonBody = objectMapper.writeValueAsString(voteRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            baseUrl
                                    + "/api/comments/"
                                    + commentId
                                    + "/vote"
                    ))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);

            return objectMapper.readValue(response.body(), CommentDto.class);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to connect to the backend",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "The HTTP request was interrupted",
                    exception
            );
        }
    }
    private void ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() > 299) {
            throw new IllegalStateException(
                    "Request failed with status "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }
    }
}
