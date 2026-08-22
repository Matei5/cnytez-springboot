package com.cnytez.app.service;

import com.cnytez.app.exception.ImageServerFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    private ImageUploadService imageUploadService;
    private RestClient restClient;
    private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        imageUploadService = new ImageUploadService("http://localhost:8080", restClient);
    }

    private void stubRequestPipeline() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(any(String.class))).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.body(any(MultiValueMap.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    void sendImageToServer_EmptyFile_ThrowsIllegalArgumentException() {
        // arrange
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        // act & assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.sendImageToServer(emptyFile, "filter");
        });

        assertEquals("File must not be empty", exception.getMessage());
    }

    @Test
    void sendImageToServer_Success() {
        // arrange
        stubRequestPipeline();
        when(responseSpec.body(String.class)).thenReturn("http://example.com/image.png");

        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());

        // act
        String result = imageUploadService.sendImageToServer(file, "filter");

        // assert
        assertEquals("http://example.com/image.png", result);
    }

    @Test
    void sendImageToServer_QuotedUrl_RemovesSurroundingQuotes() {
        // arrange
        stubRequestPipeline();
        when(responseSpec.body(String.class)).thenReturn("\"http://example.com/image.png\"");
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test".getBytes()
        );

        // act
        String result = imageUploadService.sendImageToServer(file, "filter");

        // assert
        assertEquals("http://example.com/image.png", result);
    }

    @Test
    void sendImageToServer_EmptyResponse_ThrowsImageServerFailureException() {
        // arrange
        stubRequestPipeline();
        when(responseSpec.body(String.class)).thenReturn(null);
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test".getBytes()
        );

        // act & assert
        var exception = assertThrows(
                ImageServerFailureException.class,
                () -> imageUploadService.sendImageToServer(file, "filter")
        );
        assertEquals("Image server returned an empty response", exception.getMessage());
    }
}
