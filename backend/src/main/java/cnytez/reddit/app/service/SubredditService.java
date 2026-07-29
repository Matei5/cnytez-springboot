package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.CreateSubredditRequest;
import cnytez.reddit.app.dto.SubredditDto;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.model.Subreddit;
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
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;

    public List<SubredditDto> getAllSubreddits() {
        return subredditRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public SubredditDto getSubredditById(Long id) {
        return toDto(findSubredditById(id));
    }

    public SubredditDto getSubredditByName(String name) {
        return toDto(subredditRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found: r/" + name)));
    }

    @Transactional
    public SubredditDto createSubreddit(CreateSubredditRequest request) {
        if (subredditRepository.existsByName(request.name())) {
            throw new BadRequestException("Subreddit r/" + request.name() + " already exists.");
        }

        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.ownerId()));

        Subreddit subreddit = Subreddit.builder()
                .name(request.name())
                .photo(request.photo())
                .banner(request.banner())
                .owner(owner)
                .creationDate(LocalDateTime.now())
                .build();

        // Owner automatically becomes a member
        subreddit.addMember(owner);

        return toDto(subredditRepository.save(subreddit));
    }

    @Transactional
    public SubredditDto joinSubreddit(Long subredditId, Long userId) {
        Subreddit subreddit = findSubredditById(subredditId);
        User user = userRepository.findByIdAndDeletionDateIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        subreddit.addMember(user);
        return toDto(subredditRepository.save(subreddit));
    }

    @Transactional
    public SubredditDto leaveSubreddit(Long subredditId, Long userId) {
        Subreddit subreddit = findSubredditById(subredditId);
        User user = userRepository.findByIdAndDeletionDateIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (subreddit.getOwner().getId().equals(userId)) {
            throw new BadRequestException("The owner cannot leave their own subreddit.");
        }

        subreddit.removeMember(user);
        return toDto(subredditRepository.save(subreddit));
    }

    Subreddit findSubredditById(Long id) {
        return subredditRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found with id: " + id));
    }

    private SubredditDto toDto(Subreddit s) {
        return new SubredditDto(
                s.getId(),
                s.getName(),
                s.getPhoto(),
                s.getBanner(),
                s.getOwner().getId(),
                s.getOwner().getUsername(),
                s.getCreationDate(),
                s.getMembers().size()
        );
    }
}
