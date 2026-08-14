package com.cnytez.app.service;

import com.cnytez.app.exception.UnauthorizedException;
import com.cnytez.app.model.User;
import com.cnytez.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        return findCurrentUser()
                .orElseThrow(() -> new UnauthorizedException(
                        "Authentication required."
                ));
    }

    public Optional<User> findCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String username = authentication.getName();

        return userRepository
                .findByUsernameAndDeletedAtIsNull(username);
    }
}