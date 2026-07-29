package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.LoginRequest;
import cnytez.reddit.app.dto.RegisterRequest;
import cnytez.reddit.app.dto.UserDto;
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

    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeletionDateIsNull(request.username())) {
            throw new BadRequestException("Username '" + request.username() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email '" + request.email() + "' is already registered.");
        }

        User user = User.builder()
                .name(request.name())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .profilePhoto(request.profilePhoto())
                .build();

        User saved = userRepository.save(user);
        logManager.log("Register success! User with id " + user.getId() + " registered");
        return toDto(saved);
    }

    public UserDto login(LoginRequest request) {
        User user = userRepository.findByUsernameAndDeletionDateIsNull(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password.");
        }

        logManager.log("Login success! User with id " + user.getId() + " logged in");
        return toDto(user);
    }

    private UserDto toDto(User user) {
        String name = null;

        if (user.getDeletionDate() != null) {
            name = "[deleted]";
        } else {
            name = user.getName();
        }

        return new UserDto(user.getId(), name, user.getUsername(),
                user.getEmail(), user.getProfilePhoto());
    }
}
