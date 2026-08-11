package com.cnytez.app.service;

import com.cnytez.app.exception.ImageServerFailureException;
import com.cnytez.app.exception.RejectedFileException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;

@Service
public class ImageUploadService {
    private final RestClient restClient;
    private final String imageServerUrl;

    public ImageUploadService(
            @Value("${image-server.url}") String imageServerUrl
            // takes it from YAML
    ) {
        this.restClient = RestClient.create();
        this.imageServerUrl = imageServerUrl;
    }

    public String sendImageToServer(MultipartFile file, String filterName) {
        String targetUrl = imageServerUrl + "/" + filterName;
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        String url = restClient.post()
                .uri(targetUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
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

        // Remove extra quotes from the beginning and the end
        if (url != null) {
            url = url.substring(1, url.length() - 1);
        }

        return url;
    }
}
