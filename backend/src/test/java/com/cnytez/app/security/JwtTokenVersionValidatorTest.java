package com.cnytez.app.security;

import com.cnytez.app.model.User;
import com.cnytez.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenVersionValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtTokenVersionValidator validator;

    @Test
    void validate_matchingVersions_acceptsToken() {
        User user = User.builder()
                .username("testuser")
                .tokenVersion(2)
                .build();

        Jwt jwt = createJwt(2);

        when(userRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                .thenReturn(Optional.of(user));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertFalse(result.hasErrors());
    }

    @Test
    void validate_differentVersions_rejectsToken() {
        User user = User.builder()
                .username("testuser")
                .tokenVersion(3)
                .build();

        Jwt jwt = createJwt(2);

        when(userRepository.findByUsernameAndDeletedAtIsNull("testuser"))
                .thenReturn(Optional.of(user));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertTrue(result.hasErrors());
    }

    private Jwt createJwt(int tokenVersion) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .claim("sub", "testuser")
                .claim("tokenVersion", tokenVersion)
                .build();
    }
}