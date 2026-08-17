package com.cnytez.app.service;

import com.cnytez.app.dto.internal.UserProfileDto;
import com.cnytez.app.dto.request.LoginRequest;
import com.cnytez.app.dto.request.RegisterRequest;
import com.cnytez.app.dto.response.AuthResponse;
import com.cnytez.app.dto.response.AuthUserDto;
import com.cnytez.app.exception.BadRequestException;
import com.cnytez.app.exception.UnauthorizedException;
import com.cnytez.app.logging.LogManager;
import com.cnytez.app.mapper.AuthMapper;
import com.cnytez.app.model.User;
import com.cnytez.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import com.cnytez.app.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LogManager logManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        // arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        User savedUser = User.builder().id(UUID.randomUUID()).username("testuser").email("test@example.com").build();
        AuthResponse mockResponse = new AuthResponse("fake-token", new AuthUserDto("testuser", "test@example.com"));

        when(userRepository.existsByUsernameAndDeletedAtIsNull(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser.getUsername())).thenReturn("fake-token");
        when(authMapper.toAuthResponse(savedUser, "fake-token")).thenReturn(mockResponse);

        // act
        AuthResponse response = authService.register(request);

        // assert
        assertNotNull(response);
        assertEquals("testuser", response.user().username());
        assertEquals("fake-token", response.accessToken());
        
        verify(userRepository).save(any(User.class));
        verify(logManager).log(anyString(), any());
    }

    @Test
    void register_usernameTaken_throwsConflictException() {
        // arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userRepository.existsByUsernameAndDeletedAtIsNull(request.username())).thenReturn(true);

        // act & assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            authService.register(request);
        });
        
        assertTrue(exception.getMessage().contains("already taken"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        // arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        User user = User.builder().id(UUID.randomUUID()).username("testuser").password("encoded-password").build();
        AuthResponse mockResponse = new AuthResponse("fake-token", new AuthUserDto("testuser", "test@example.com"));

        when(userRepository.findByUsernameAndDeletedAtIsNull(request.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getUsername())).thenReturn("fake-token");
        when(authMapper.toAuthResponse(user, "fake-token")).thenReturn(mockResponse);

        // act
        AuthResponse response = authService.login(request);

        // assert
        assertNotNull(response);
        assertEquals("fake-token", response.accessToken());
    }

    @Test
    void login_invalidUsername_throwsUnauthorizedException() {
        // arrange
        LoginRequest request = new LoginRequest("wronguser", "password123");
        when(userRepository.findByUsernameAndDeletedAtIsNull(request.username())).thenReturn(Optional.empty());

        // act & assert
        assertThrows(UnauthorizedException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    void getProfile_success() {
        // arrange
        User user = User.builder().id(UUID.randomUUID()).username("testuser").build();
        UserProfileDto mockDto = new UserProfileDto("testuser", "test@example.com", "Test User", null);
        
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(authMapper.toProfileDto(user)).thenReturn(mockDto);

        // act
        UserProfileDto result = authService.getProfile();

        // assert
        assertNotNull(result);
        assertEquals("testuser", result.username());
    }

    @Test
    void updateProfile_success() {
        // arrange
        com.cnytez.app.dto.request.UpdateProfileRequest request = new com.cnytez.app.dto.request.UpdateProfileRequest("New Display Name", "http://example.com/photo.jpg");
        User user = User.builder().id(UUID.randomUUID()).username("testuser").name("Old Name").build();
        UserProfileDto mockDto = new UserProfileDto("testuser", "test@example.com", "New Display Name", "http://example.com/photo.jpg");

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authMapper.toProfileDto(user)).thenReturn(mockDto);

        // act
        UserProfileDto result = authService.updateProfile(request);

        // assert
        assertNotNull(result);
        assertEquals("New Display Name", result.displayName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_success() {
        // arrange
        com.cnytez.app.dto.request.ChangePasswordRequest request = new com.cnytez.app.dto.request.ChangePasswordRequest("oldpass", "newpass");
        User user = User.builder().id(UUID.randomUUID()).username("testuser").password("encoded-oldpass").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("oldpass", "encoded-oldpass")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-newpass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // act
        authService.changePassword(request);

        // assert
        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_invalidCurrentPassword_throwsBadRequestException() {
        // arrange
        com.cnytez.app.dto.request.ChangePasswordRequest request = new com.cnytez.app.dto.request.ChangePasswordRequest("wrongpass", "newpass");
        User user = User.builder().id(UUID.randomUUID()).username("testuser").password("encoded-oldpass").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "encoded-oldpass")).thenReturn(false);

        // act & assert
        assertThrows(BadRequestException.class, () -> authService.changePassword(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_success() {
        // arrange
        com.cnytez.app.dto.request.DeleteUserRequest request = new com.cnytez.app.dto.request.DeleteUserRequest("password123");
        User user = User.builder().id(UUID.randomUUID()).username("testuser").password("encoded-password").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // act
        authService.deleteUser(request);

        // assert
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_invalidPassword_throwsBadRequestException() {
        // arrange
        com.cnytez.app.dto.request.DeleteUserRequest request = new com.cnytez.app.dto.request.DeleteUserRequest("wrongpass");
        User user = User.builder().id(UUID.randomUUID()).username("testuser").password("encoded-password").build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "encoded-password")).thenReturn(false);

        // act & assert
        assertThrows(BadRequestException.class, () -> authService.deleteUser(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
