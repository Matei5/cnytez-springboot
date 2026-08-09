package com.cnytez.cli.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cnytez.cli.dto.CommentDto;
import com.cnytez.cli.dto.CreateCommentRequest;
import com.cnytez.cli.dto.CreatePostRequest;
import com.cnytez.cli.dto.CreateSubredditRequest;
import com.cnytez.cli.dto.LoginRequest;
import com.cnytez.cli.dto.PostDto;
import com.cnytez.cli.dto.RegisterRequest;
import com.cnytez.cli.dto.SubredditDto;
import com.cnytez.cli.dto.UserDto;
import com.cnytez.cli.dto.VoteRequest;

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

    public List<PostDto> getPostsBySubreddit(Long subredditId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl + "/api/posts/by-subreddit/" + subredditId
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

    public void deletePost(Long postId, Long requestingUserId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl
                                + "/api/posts/"
                                + postId
                                + "?requestingUserId="
                                + requestingUserId
                ))
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);
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

    public void deleteComment(Long commentId, Long requestingUserId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        baseUrl
                                + "/api/comments/"
                                + commentId
                                + "?requestingUserId="
                                + requestingUserId
                ))
                .DELETE()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            ensureSuccess(response);
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
