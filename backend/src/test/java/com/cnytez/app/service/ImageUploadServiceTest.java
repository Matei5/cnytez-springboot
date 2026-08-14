package com.cnytez.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    private ImageUploadService imageUploadService;

    @BeforeEach
    void setUp() {
        imageUploadService = new ImageUploadService("http://localhost:8080");
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
    void sendImageToServer_Success() throws Exception {
        // arrange
        org.springframework.web.client.RestClient restClientMock = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.class);
        org.springframework.web.client.RestClient.RequestBodyUriSpec uriSpecMock = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.RequestBodyUriSpec.class);
        org.springframework.web.client.RestClient.RequestBodySpec bodySpecMock = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.RequestBodySpec.class);
        org.springframework.web.client.RestClient.ResponseSpec responseSpecMock = org.mockito.Mockito.mock(org.springframework.web.client.RestClient.ResponseSpec.class);
        
        java.lang.reflect.Field field = ImageUploadService.class.getDeclaredField("restClient");
        field.setAccessible(true);
        field.set(imageUploadService, restClientMock);

        org.mockito.Mockito.when(restClientMock.post()).thenReturn(uriSpecMock);
        org.mockito.Mockito.when(uriSpecMock.uri(org.mockito.ArgumentMatchers.anyString())).thenReturn(bodySpecMock);
        org.mockito.Mockito.when(bodySpecMock.contentType(org.mockito.ArgumentMatchers.any())).thenReturn(bodySpecMock);
        org.mockito.Mockito.when(bodySpecMock.body(org.mockito.ArgumentMatchers.any(org.springframework.util.MultiValueMap.class))).thenReturn(bodySpecMock);
        org.mockito.Mockito.when(bodySpecMock.retrieve()).thenReturn(responseSpecMock);
        org.mockito.Mockito.when(responseSpecMock.onStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(responseSpecMock);
        org.mockito.Mockito.when(responseSpecMock.body(String.class)).thenReturn("\"http://example.com/image.png\"");

        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());

        // act
        String result = imageUploadService.sendImageToServer(file, "filter");

        // assert
        assertEquals("http://example.com/image.png", result);
    }
}
