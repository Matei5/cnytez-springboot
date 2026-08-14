package com.cnytez.app.service;

import com.cnytez.app.exception.UnauthorizedException;
import com.cnytez.app.model.User;
import com.cnytez.app.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findCurrentUser_AuthenticatedUser_ReturnsUser() {
        // arrange
        String username = "testuser";
        User user = User.builder().username(username).build();
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(username, "password", Collections.emptyList());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        
        when(userRepository.findByUsernameAndDeletedAtIsNull(username)).thenReturn(Optional.of(user));

        // act
        Optional<User> result = currentUserService.findCurrentUser();

        // assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }

    @Test
    void findCurrentUser_NotAuthenticated_ReturnsEmpty() {
        // arrange
        SecurityContextHolder.clearContext();

        // act
        Optional<User> result = currentUserService.findCurrentUser();

        // assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUser_Authenticated_ReturnsUser() {
        // arrange
        String username = "testuser";
        User user = User.builder().username(username).build();
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(username, "password", Collections.emptyList());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        
        when(userRepository.findByUsernameAndDeletedAtIsNull(username)).thenReturn(Optional.of(user));

        // act
        User result = currentUserService.getCurrentUser();

        // assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    void getCurrentUser_NotAuthenticated_ThrowsUnauthorizedException() {
        // arrange
        SecurityContextHolder.clearContext();

        // act & assert
        assertThrows(UnauthorizedException.class, () -> currentUserService.getCurrentUser());
    }
}
