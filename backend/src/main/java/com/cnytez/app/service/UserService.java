package com.cnytez.app.service;

import com.cnytez.app.dto.request.UpdateProfileRequest;
import com.cnytez.app.dto.internal.UserDto;
import com.cnytez.app.exception.BadRequestException;
import com.cnytez.app.exception.ResourceNotFoundException;
import com.cnytez.app.log.LogManager;
import com.cnytez.app.mapper.UserMapper;
import com.cnytez.app.model.Subreddit;
import com.cnytez.app.model.User;
import com.cnytez.app.repository.SubredditRepository;
import com.cnytez.app.repository.UserRepository;
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
    private final UserMapper userMapper;

    public List<UserDto> getAllUsers() {
        return userRepository.findAllByDeletionDateIsNull().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUserById(UUID id) {
        return userMapper.toDto(userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id)));
    }

    public UserDto getUserByUsername(String username) {
        return userMapper.toDto(userRepository.findByUsernameAndDeletionDateIsNull(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    @Transactional
    public UserDto updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletionDateIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));


        if (request.displayName() != null) {
            user.setName(request.displayName());
        }

        if (request.profilePhotoURL() != null) {
            user.setProfilePhotoURL(request.profilePhotoURL());
        }

        logManager.log("Update profile success! User with id " + id + " updated");
        return userMapper.toDto(userRepository.save(user));
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
        user.setProfilePhotoURL(null);

        user.setDeletionDate(LocalDateTime.now());

        userRepository.save(user);
        logManager.log("Delete user success! User with id " + id + " deleted");
    }
}
