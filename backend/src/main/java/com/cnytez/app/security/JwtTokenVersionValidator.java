package com.cnytez.app.security;

import com.cnytez.app.model.User;
import com.cnytez.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenVersionValidator
        implements OAuth2TokenValidator<Jwt> {

    private final UserRepository userRepository;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String username = jwt.getSubject();
        Number tokenVersion = jwt.getClaim("tokenVersion");

        if (username == null || tokenVersion == null) {
            return invalidToken();
        }

        Optional<User> user = userRepository
                .findByUsernameAndDeletedAtIsNull(username);

        if (user.isEmpty()) {
            return invalidToken();
        }

        if (user.get().getTokenVersion() != tokenVersion.intValue()) {
            return invalidToken();
        }

        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult invalidToken() {
        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "The token is no longer valid.",
                null
        );

        return OAuth2TokenValidatorResult.failure(error);
    }
}