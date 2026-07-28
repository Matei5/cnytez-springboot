package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.LoginRequest;
import cnytez.reddit.app.dto.RegisterRequest;
import cnytez.reddit.app.dto.UserDto;
import cnytez.reddit.app.exception.UnauthorizedException;
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

    public UserDto register(RegisterRequest request) {

        User user = User.builder()
                .name(request.name())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .profilePhoto(request.profilePhoto())
                .build();

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        return toDto(user);
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getUsername(),
                user.getEmail(), user.getProfilePhoto());
    }
}
