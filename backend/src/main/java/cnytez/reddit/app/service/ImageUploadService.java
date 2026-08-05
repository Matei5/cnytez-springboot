package cnytez.reddit.app.service;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.rmi.ServerException;

@Service
public class ImageUploadService {
    private final RestClient restClient;

    public ImageUploadService() {
        this.restClient = RestClient.create();
    }

    public String sendImageToServer(MultipartFile file, Integer filter) {
        String targetUrl = "http://ec2-18-193-138-107.eu-central-1.compute.amazonaws.com:8123/";
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (filter != null) {
            targetUrl = targetUrl + filter;
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        return restClient.post()
                .uri(targetUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("Invalid request: " + response.getStatusCode() + response.getBody());
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new RuntimeException("Internal server issue: " + response.getStatusCode() + response.getBody().toString());
                }))
                .body(String.class);
    }
}
