package com.cnytez.app.contract;

import com.cnytez.app.dto.request.CreateSubredditRequest;
import com.cnytez.app.dto.request.RegisterRequest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("cross-service")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ImageServerRoundTripTest {

    private static final ImageServerProcess IMAGE_SERVER = ImageServerProcess.start();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:image-contract;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.defer-datasource-initialization", () -> "true");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.data-locations", () -> "classpath:test-data.sql");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("image-server.url", IMAGE_SERVER::baseUrl);
    }

    @AfterAll
    static void stopImageServer() {
        IMAGE_SERVER.close();
    }

    @Test
    void createPostSendsImageToImageServerAndReturnsProcessedUrl() throws Exception {
        String token = registerUser();
        String subreddit = "images_" + UUID.randomUUID().toString().substring(0, 8);

        CreateSubredditRequest subredditRequest = new CreateSubredditRequest(
                subreddit,
                "Image contract test",
                "Subreddit for the cross-service image test",
                null
        );

        mockMvc.perform(post("/subreddits")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subredditRequest)))
                .andExpect(status().isCreated());

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "pixel.png",
                MediaType.IMAGE_PNG_VALUE,
                createPng()
        );

        String response = mockMvc.perform(multipart("/posts")
                        .file(image)
                        .param("title", "Cross-service image post")
                        .param("content", "The image should make a complete round trip")
                        .param("subreddit", subreddit)
                        .param("filter", "1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.imageUrl")
                        .value("https://example.test/processed-image.jpeg"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = JsonPath.read(response, "$.data.id");

        mockMvc.perform(get("/posts/{id}", postId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.imageUrl")
                        .value("https://example.test/processed-image.jpeg"));
    }

    private String registerUser() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest request = new RegisterRequest(
                "image_user_" + uniqueId,
                uniqueId + "@example.com",
                "StrongPassword123!"
        );

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.data.accessToken");
    }

    private static byte[] createPng() throws IOException {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, Color.RED.getRGB());
            }
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        }
    }

    private static final class ImageServerProcess {

        private static final int LOG_LIMIT = 100;
        private final Process process;
        private final String baseUrl;
        private final Deque<String> output = new ArrayDeque<>();

        private ImageServerProcess(Process process, String baseUrl) {
            this.process = process;
            this.baseUrl = baseUrl;
        }

        static ImageServerProcess start() {
            try {
                int port = findFreePort();
                String baseUrl = "http://127.0.0.1:" + port;
                Path project = findRepositoryRoot().resolve(
                        "image-server/ImageProcessingServer.ContractHost/ImageProcessingServer.ContractHost.csproj"
                );

                Process process = new ProcessBuilder(
                        "dotnet",
                        "run",
                        "--project",
                        project.toString(),
                        "--configuration",
                        "Release",
                        "--no-launch-profile",
                        "--",
                        "--urls",
                        baseUrl
                )
                        .redirectErrorStream(true)
                        .start();

                ImageServerProcess server = new ImageServerProcess(process, baseUrl);
                server.captureOutput();
                server.awaitReadiness();
                return server;
            } catch (IOException e) {
                throw new IllegalStateException("Could not start the image-server contract host", e);
            }
        }

        String baseUrl() {
            return baseUrl;
        }

        void close() {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
            try {
                if (!process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.descendants().forEach(ProcessHandle::destroyForcibly);
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
        }

        private void captureOutput() {
            Thread.ofPlatform()
                    .daemon()
                    .name("image-server-contract-output")
                    .start(() -> {
                        try (var reader = process.inputReader()) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                synchronized (output) {
                                    if (output.size() == LOG_LIMIT) {
                                        output.removeFirst();
                                    }
                                    output.addLast(line);
                                }
                            }
                        } catch (IOException ignored) {
                        }
                    });
        }

        private void awaitReadiness() {
            Instant deadline = Instant.now().plus(Duration.ofSeconds(90));
            while (Instant.now().isBefore(deadline)) {
                if (!process.isAlive()) {
                    throw new IllegalStateException("Image-server contract host stopped during startup:\n" + recentOutput());
                }

                try {
                    HttpURLConnection connection = (HttpURLConnection) URI.create(
                            baseUrl + "/health/ready"
                    ).toURL().openConnection();
                    connection.setConnectTimeout(500);
                    connection.setReadTimeout(500);
                    if (connection.getResponseCode() == 200) {
                        connection.disconnect();
                        return;
                    }
                    connection.disconnect();
                } catch (IOException ignored) {
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    close();
                    throw new IllegalStateException("Interrupted while waiting for the image server", e);
                }
            }

            close();
            throw new IllegalStateException("Image-server contract host did not become ready:\n" + recentOutput());
        }

        private String recentOutput() {
            synchronized (output) {
                return String.join(System.lineSeparator(), output);
            }
        }

        private static int findFreePort() throws IOException {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            }
        }

        private static Path findRepositoryRoot() {
            Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
            while (current != null) {
                if (Files.isDirectory(current.resolve("backend"))
                        && Files.isDirectory(current.resolve("image-server"))) {
                    return current;
                }
                current = current.getParent();
            }
            throw new IllegalStateException("Could not locate the repository root");
        }
    }
}
