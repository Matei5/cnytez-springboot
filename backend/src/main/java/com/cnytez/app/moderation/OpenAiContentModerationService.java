package com.cnytez.app.moderation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiContentModerationService implements ContentModerationService {

    private final boolean enabled;
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final RestClient restClient;

    @Autowired
    public OpenAiContentModerationService(
            @Value("${content-moderation.enabled:false}") boolean enabled,
            @Value("${content-moderation.openai.api-key:}") String apiKey,
            @Value("${content-moderation.openai.url}") String apiUrl,
            @Value("${content-moderation.openai.model}") String model,
            @Value("${content-moderation.openai.timeout:2s}") Duration timeout
    ) {
        this(enabled, apiKey, apiUrl, model, createRestClient(timeout));
    }   

    OpenAiContentModerationService(
            boolean enabled,
            String apiKey,
            String apiUrl,
            String model,
            RestClient restClient
    ) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.restClient = restClient;
    }

    @Override
    public ModerationResult moderate(String title, String content) {
        if (!enabled) {
            return new ModerationResult(
                    ModerationStatus.DISABLED,
                    "Content moderation is disabled."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            return unavailable("The moderation API key is missing.");
        }

        String input = "Title: " + title + "\nContent: " + (content == null ? "" : content);
        ModerationApiRequest request = new ModerationApiRequest(model, input);

        try {
            ModerationApiResponse response = restClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .body(request)
                    .retrieve()
                    .body(ModerationApiResponse.class);

            if (response == null || response.results() == null || response.results().isEmpty()) {
                return unavailable("The moderation API returned an empty response.");
            }

            ModerationApiResult result = response.results().getFirst();

            if (!result.flagged()) {
                return new ModerationResult(
                        ModerationStatus.APPROVED,
                        "Content approved."
                );
            }

            return new ModerationResult(
                    ModerationStatus.REJECTED,
                    findFlaggedCategories(result.categories())
            );
        } catch (RestClientException exception) {
            return unavailable("The moderation API request failed.");
        }
    }

    private static RestClient createRestClient(Duration timeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    private ModerationResult unavailable(String reason) {
        return new ModerationResult(ModerationStatus.UNAVAILABLE, reason);
    }

    private String findFlaggedCategories(Map<String, Boolean> categories) {
        if (categories == null || categories.isEmpty()) {
            return "unspecified";
        }

        String flaggedCategories = categories.entrySet().stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .sorted()
                .reduce((first, second) -> first + ", " + second)
                .orElse("unspecified");

        return flaggedCategories;
    }

    private record ModerationApiRequest(
            String model,
            String input
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModerationApiResponse(
            List<ModerationApiResult> results
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModerationApiResult(
            boolean flagged,
            Map<String, Boolean> categories
    ) {
    }
}
