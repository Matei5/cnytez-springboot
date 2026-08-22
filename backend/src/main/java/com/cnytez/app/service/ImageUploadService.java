package com.cnytez.app.service;

import com.cnytez.app.exception.ImageServerFailureException;
import com.cnytez.app.exception.RejectedFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
public class ImageUploadService {
    private final RestClient restClient;
    private final String imageServerUrl;

    @Autowired
    public ImageUploadService(
            @Value("${image-server.url}") String imageServerUrl
    ) {
        this(imageServerUrl, RestClient.create());
    }

    ImageUploadService(String imageServerUrl, RestClient restClient) {
        this.imageServerUrl = imageServerUrl;
        this.restClient = restClient;
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
                    throw new RejectedFileException("Invalid file: " + response.getStatusCode() + " " + errorBody);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new ImageServerFailureException("Internal server issue: " + response.getStatusCode() + " " + errorBody);
                })
                .body(String.class);

        if (url == null || url.isBlank()) {
            throw new ImageServerFailureException("Image server returned an empty response");
        }

        if (url.length() >= 2 && url.startsWith("\"") && url.endsWith("\"")) {
            return url.substring(1, url.length() - 1);
        }

        return url;
    }
}
