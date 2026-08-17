package com.cnytez.app.service;

import com.cnytez.app.dto.request.*;
import com.cnytez.app.dto.response.AuthResponse;
import com.cnytez.app.exception.BadRequestException;
import com.cnytez.app.exception.ConflictException;
import com.cnytez.app.exception.UnauthorizedException;
import com.cnytez.app.logging.LogLevel;
import com.cnytez.app.logging.LogManager;
import com.cnytez.app.mapper.AuthMapper;
import com.cnytez.app.model.User;
import com.cnytez.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.cnytez.app.dto.internal.UserProfileDto;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogManager logManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final AuthMapper authMapper;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email '" + request.email() + "' is already registered.");
        }

        User user = User.builder()
                .name(request.username())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .profilePhotoUrl(null)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();

        User saved = userRepository.save(user);
        logManager.log("Register success! User with id " + user.getId() + " registered", LogLevel.INFO);
        String token = jwtService.generateToken(saved.getUsername());
        return authMapper.toAuthResponse(saved, token);
    }

    public AuthResponse  login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletedAtIsNull(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        logManager.log("Login success! User with id " + user.getId() + " logged in", LogLevel.INFO);
        String token = jwtService.generateToken(user.getUsername());
        return authMapper.toAuthResponse(user, token);
    }

    public UserProfileDto getProfile() {
        User user = currentUserService.getCurrentUser();
        return authMapper.toProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();

        if (request.displayName() != null) {
            user.setName(request.displayName());
        }

        if (request.avatarUrl() != null) {
            user.setProfilePhotoUrl(request.avatarUrl());
        }

        user.setUpdatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        logManager.log(
                "Update profile success! User with id "
                        + user.getId()
                        + " updated profile",
                LogLevel.INFO
        );

        return authMapper.toProfileDto(savedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {
            throw new BadRequestException(
                    "Current password is incorrect."
            );
        }

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        logManager.log(
                "Password change success! User with id "
                        + user.getId()
                        + " changed password",
                LogLevel.INFO
        );
    }

    public void deleteUser(DeleteUserRequest request){
        User user = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BadRequestException(
                    "Current password is incorrect."
            );
        }

        user.setDeletedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        logManager.log(
                "User with id "
                        + user.getId()
                        + " deleted successfully!",
                LogLevel.INFO
        );
    }
}
