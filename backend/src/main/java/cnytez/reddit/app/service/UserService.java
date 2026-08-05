package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.UpdateProfileRequest;
import cnytez.reddit.app.dto.UserDto;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.Subreddit;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.repository.SubredditRepository;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final LogManager logManager;

    public List<UserDto> getAllUsers() {
        return userRepository.findAllByDeletionDateIsNull().stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getUserById(UUID id) {
        return toDto(userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    public UserDto getUserByUsername(String username) {
        return toDto(userRepository.findByUsernameAndDeletionDateIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    @Transactional
    public UserDto updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));


        if (request.displayName() != null) {
            user.setName(request.displayName());
        }

        if (request.avatarUrl() != null) {
            user.setProfilePhoto(request.avatarUrl());
        }

        logManager.log("Update profile success! User with id " + id + " updated");
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!subredditRepository.findByOwnerId(id).isEmpty()) {
            throw new BadRequestException("User is owner of one or more subreddits");
        }

        List<Subreddit> userSubreddits = subredditRepository.findAllByMembersId(id);

        for (Subreddit subreddit : userSubreddits) {
            subreddit.removeMember(user);
        }

        subredditRepository.saveAll(userSubreddits);

        user.setName(null);
        user.setUsername("[deleted_" + user.getId() + "]");
        user.setEmail("[deleted_" + user.getId() + "]");
        user.setPassword("deleted");
        user.setProfilePhoto(null);

        user.setDeletionDate(LocalDateTime.now());

        userRepository.save(user);
        logManager.log("Delete user success! User with id " + id + " deleted");
    }

    private UserDto toDto(User user) {
        String username = null;

        if (user.getDeletionDate() != null) {
            username = "[deleted]";
        } else {
            username = user.getUsername();
        }

        return new UserDto(user.getId(), user.getName(), username,
                user.getEmail(), user.getProfilePhoto());
    }
}
