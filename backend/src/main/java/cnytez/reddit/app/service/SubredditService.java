package cnytez.reddit.app.service;

import cnytez.reddit.app.request.CreateSubredditRequest;
import cnytez.reddit.app.dto.SubredditDto;
import cnytez.reddit.app.request.UpdateSubredditRequest;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.Subreddit;
import cnytez.reddit.app.model.User;
import cnytez.reddit.app.repository.PostRepository;
import cnytez.reddit.app.repository.SubredditRepository;
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


    public List<SubredditDto> getAllSubreddits() {
        return subredditRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public SubredditDto getSubredditByName(String name) {
        return toDto(findSubredditByName(name));
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

        return toDto(savedSubreddit);
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

        return toDto(savedSubreddit);
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

    private SubredditDto toDto(Subreddit subreddit) {
        long postCount =
                postRepository.countBySubreddit(subreddit);

        return new SubredditDto(
                subreddit.getId(),
                subreddit.getName(),
                subreddit.getDisplayName(),
                subreddit.getDescription(),
                subreddit.getMembers().size(),
                postCount,
                subreddit.getIconUrl(),
                subreddit.getCreationDate()
        );
    }
}
