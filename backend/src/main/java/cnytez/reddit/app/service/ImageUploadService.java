package cnytez.reddit.app.service;

import cnytez.reddit.app.exception.ImageServerFailureException;
import cnytez.reddit.app.exception.RejectedFileException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;

@Service
public class ImageUploadService {
    private final RestClient restClient;

    public ImageUploadService() {
        this.restClient = RestClient.create();
    }

    public String sendImageToServer(MultipartFile file, String filterName) {
        String targetUrl = "http://ec2-18-193-138-107.eu-central-1.compute.amazonaws.com:8123/" + filterName;
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource())
                .filename(file.getOriginalFilename());

        return restClient.post()
                .uri(targetUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new RejectedFileException("Invalid file: " + response.getStatusCode() + " "+ errorBody);
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ImageServerFailureException("Internal server issue: " + response.getStatusCode() + " " + errorBody);
                }))
                .body(String.class);
    }
}
