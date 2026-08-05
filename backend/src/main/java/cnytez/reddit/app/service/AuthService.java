package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.LoginRequest;
import cnytez.reddit.app.dto.RegisterRequest;
import cnytez.reddit.app.dto.AuthResponse;
import cnytez.reddit.app.dto.AuthUserDto;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.UnauthorizedException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogManager logManager;
    private final JwtService jwtService;

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

    private AuthResponse toAuthResponse(User user, String token) {
        AuthUserDto userDto = new AuthUserDto(
                user.getUsername(),
                user.getEmail()
        );

        return new AuthResponse(token, userDto);
    }
}
