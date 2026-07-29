package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.UpdateProfileRequest;
import cnytez.reddit.app.dto.UserDto;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.repository.SubredditRepository;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAllByDeletionDateIsNull().stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        return toDto(userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    public UserDto getUserByUsername(String username) {
        return toDto(userRepository.findByUsernameAndDeletionDateIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    @Transactional
    public UserDto updateProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndDeletionDateIsNull(request.username())) {
                throw new BadRequestException("Username '" + request.username() + "' is already taken.");
            }
            user.setUsername(request.username());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.profilePhoto() != null) {
            user.setProfilePhoto(request.profilePhoto());
        }

        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!subredditRepository.findByOwnerId(id).isEmpty()) {
            throw new BadRequestException("User is owner of one or more subreddits");
        }

        user.setName(null);
        user.setUsername("[deleted_" + user.getId() + "]");
        user.setEmail(null);
        user.setPassword(null);
        user.setProfilePhoto(null);

        user.setDeletionDate(LocalDateTime.now());

        userRepository.save(user);
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
