package com.cnytez.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    @Test
    void generateToken_ReturnsTokenString() {
        // arrange
        String username = "testuser";
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", username)
                .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // act
        String token = jwtService.generateToken(username);

        // assert
        assertEquals("mock-token", token);
    }
}
