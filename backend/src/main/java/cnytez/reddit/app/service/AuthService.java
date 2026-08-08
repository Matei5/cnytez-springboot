package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.request.LoginRequest;
import cnytez.reddit.app.dto.request.RegisterRequest;
import cnytez.reddit.app.dto.response.AuthResponse;
import cnytez.reddit.app.dto.response.AuthUserDto;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.UnauthorizedException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cnytez.reddit.app.dto.request.ChangePasswordRequest;
import cnytez.reddit.app.dto.request.UpdateProfileRequest;
import cnytez.reddit.app.dto.internal.UserProfileDto;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogManager logManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeletionDateIsNull(request.username())) {
            throw new BadRequestException("Username '" + request.username() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email '" + request.email() + "' is already registered.");
        }

        User user = User.builder()
                .name(request.username())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .profilePhoto(null)
                .build();

        User saved = userRepository.save(user);
        logManager.log("Register success! User with id " + user.getId() + " registered");
        String token = jwtService.generateToken(saved.getUsername());
        return toAuthResponse(saved, token);
    }

    public AuthResponse  login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletionDateIsNull(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        logManager.log("Login success! User with id " + user.getId() + " logged in");
        String token = jwtService.generateToken(user.getUsername());
        return toAuthResponse(user, token);
    }

    public UserProfileDto getProfile() {
        User user = currentUserService.getCurrentUser();
        return toProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();

        if (request.displayName() != null) {
            user.setName(request.displayName());
        }

        if (request.avatarUrl() != null) {
            user.setProfilePhoto(request.avatarUrl());
        }

        User savedUser = userRepository.save(user);

        logManager.log(
                "Update profile success! User with id "
                        + user.getId()
                        + " updated profile"
        );

        return toProfileDto(savedUser);
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

        userRepository.save(user);

        logManager.log(
                "Password change success! User with id "
                        + user.getId()
                        + " changed password"
        );
    }

    private UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getProfilePhoto()
        );
    }
    private AuthResponse toAuthResponse(User user, String token) {
        AuthUserDto userDto = new AuthUserDto(
                user.getUsername(),
                user.getEmail()
        );

        return new AuthResponse(token, userDto);
    }
}
