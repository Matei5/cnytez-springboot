package com.cnytez.app.service;

import com.cnytez.app.dto.request.CreateSubredditRequest;
import com.cnytez.app.dto.internal.SubredditDto;
import com.cnytez.app.dto.request.UpdateSubredditRequest;
import com.cnytez.app.exception.BadRequestException;
import com.cnytez.app.exception.ResourceNotFoundException;
import com.cnytez.app.logging.LogManager;
import com.cnytez.app.mapper.SubredditMapper;
import com.cnytez.app.model.Subreddit;
import com.cnytez.app.model.User;
import com.cnytez.app.repository.PostRepository;
import com.cnytez.app.repository.SubredditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final CurrentUserService currentUserService;
    private final PostRepository postRepository;
    private final LogManager logManager;
    private final SubredditMapper subredditMapper;


    public List<SubredditDto> getAllSubreddits() {
        return subredditRepository.findAll().stream()
                .map(subreddit ->
                        subredditMapper.toDto(
                                subreddit,
                                getPostCount(subreddit)
                        )).toList();
    }

    public SubredditDto getSubredditByName(String name) {
        Subreddit subreddit = findSubredditByName(name);
        return subredditMapper.toDto(subreddit, getPostCount(subreddit));
    }


    @Transactional
    public SubredditDto createSubreddit(
            CreateSubredditRequest request
    ) {
        if (subredditRepository.existsByName(request.name())) {
            throw new BadRequestException(
                    "Subreddit " + request.name() + " already exists."
            );
        }

        User owner = currentUserService.getCurrentUser();

        Subreddit subreddit = Subreddit.builder()
                .name(request.name())
                .displayName(request.displayName())
                .description(request.description())
                .iconUrl(request.iconUrl())
                .owner(owner)
                .creationDate(LocalDateTime.now())
                .build();

        subreddit.addMember(owner);

        Subreddit savedSubreddit =
                subredditRepository.save(subreddit);

        logManager.log(
                "Create subreddit success! User with id "
                        + owner.getId()
                        + " created subreddit "
                        + savedSubreddit.getName()
        );

        return subredditMapper.toDto(savedSubreddit, getPostCount(subreddit));
    }

    @Transactional
    public SubredditDto updateSubreddit(
            String name,
            UpdateSubredditRequest request
    ) {
        Subreddit subreddit = findSubredditByName(name);
        User currentUser = currentUserService.getCurrentUser();

        if (!subreddit.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Only the subreddit owner can update it."
            );
        }

        if (request.displayName() != null) {
            subreddit.setDisplayName(request.displayName());
        }

        if (request.description() != null) {
            subreddit.setDescription(request.description());
        }

        if (request.iconUrl() != null) {
            subreddit.setIconUrl(request.iconUrl());
        }

        Subreddit savedSubreddit =
                subredditRepository.save(subreddit);

        return subredditMapper.toDto(savedSubreddit, getPostCount(subreddit));
    }

    @Transactional
    public void deleteSubreddit(String name) {
        Subreddit subreddit = findSubredditByName(name);
        User currentUser = currentUserService.getCurrentUser();

        if (!subreddit.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Only the subreddit owner can delete it."
            );
        }

        long postCount =
                postRepository.countBySubreddit(subreddit);

        if (postCount > 0) {
            throw new BadRequestException(
                    "A subreddit with posts cannot be deleted."
            );
        }

        subredditRepository.delete(subreddit);

        logManager.log(
                "Delete subreddit success! User with id "
                        + currentUser.getId()
                        + " deleted subreddit "
                        + name
        );
    }

    private Subreddit findSubredditByName(String name) {
        return subredditRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subreddit not found: " + name
                ));
    }

    private long getPostCount(Subreddit subreddit) {
        return postRepository.countBySubreddit(subreddit);
    }
}
