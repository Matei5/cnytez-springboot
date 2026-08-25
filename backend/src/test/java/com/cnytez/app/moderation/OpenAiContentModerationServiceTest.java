package com.cnytez.app.moderation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiContentModerationServiceTest {

    private static final String API_URL = "https://api.openai.com/v1/moderations";

    private MockRestServiceServer server;
    private OpenAiContentModerationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new OpenAiContentModerationService(
                true,
                "test-key",
                API_URL,
                "omni-moderation-2024-09-26",
                builder.build()
        );
    }

    @Test
    void moderate_safeContent_returnsApproved() {
        server.expect(once(), requestTo(API_URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("omni-moderation-2024-09-26"))
                .andExpect(jsonPath("$.input").value("Title: A normal title\nContent: A normal post"))
                .andRespond(withSuccess(
                        """
                        {
                          "results": [
                            {
                              "flagged": false,
                              "categories": {"violence": false}
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        ModerationResult result = service.moderate("A normal title", "A normal post");

        assertEquals(ModerationStatus.APPROVED, result.status());
        server.verify();
    }

    @Test
    void moderate_flaggedContent_returnsRejectedWithCategories() {
        server.expect(once(), requestTo(API_URL))
                .andRespond(withSuccess(
                        """
                        {
                          "results": [
                            {
                              "flagged": true,
                              "categories": {
                                "harassment": true,
                                "violence": true,
                                "self-harm": false
                              }
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        ModerationResult result = service.moderate("Title", "Content");

        assertEquals(ModerationStatus.REJECTED, result.status());
        assertEquals("harassment, violence", result.reason());
        server.verify();
    }

    @Test
    void moderate_apiFailure_returnsUnavailable() {
        server.expect(once(), requestTo(API_URL))
                .andRespond(withServerError());

        ModerationResult result = service.moderate("Title", "Content");

        assertEquals(ModerationStatus.UNAVAILABLE, result.status());
        server.verify();
    }

    @Test
    void moderate_disabled_doesNotCallApi() {
        OpenAiContentModerationService disabledService = new OpenAiContentModerationService(
                false,
                "",
                API_URL,
                "omni-moderation-2024-09-26",
                RestClient.create()
        );

        ModerationResult result = disabledService.moderate("Title", "Content");

        assertEquals(ModerationStatus.DISABLED, result.status());
    }
}
