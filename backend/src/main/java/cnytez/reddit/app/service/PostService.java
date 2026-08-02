package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.UpdatePostRequest;
import cnytez.reddit.app.dto.CreatePostRequest;
import cnytez.reddit.app.dto.PostDto;
import cnytez.reddit.app.dto.VoteRequest;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.*;
import cnytez.reddit.app.repository.CommentRepository;
import cnytez.reddit.app.repository.PostRepository;
import cnytez.reddit.app.repository.PostVoteRepository;
import cnytez.reddit.app.repository.SubredditRepository;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private static final String CURRENT_USERNAME = "current_user";
    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;
    private final LogManager logManager;

    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreationDateDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public List<PostDto> getPostsBySubreddit(UUID subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found with id: " + subredditId));
        return postRepository.findBySubredditOrderByCreationDateDesc(subreddit).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PostDto> getPostsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return postRepository.findByOwner(user).stream()
                .map(this::toDto)
                .toList();
    }

    public PostDto getPostById(UUID id) {
        return toDto(findPostById(id));
    }

    @Transactional
    public PostDto createPost(CreatePostRequest request) {
        User owner = userRepository.findByUsernameAndDeletionDateIsNull(request.author())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + request.author()));
        Subreddit subreddit = subredditRepository.findByName(request.subreddit())
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found with name: " + request.subreddit()));

        Post post = Post.builder()
                .title(request.title())
                .text(request.content())
                .image(null)
                .owner(owner)
                .subreddit(subreddit)
                .creationDate(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);

        PostVote creatorVote = PostVote.builder()
                .user(owner)
                .post(savedPost)
                .voteType(VoteType.UPVOTE)
                .build();
        postVoteRepository.save(creatorVote);
        logManager.log("Create post success! User with id " + owner.getId() +
                " created post with id " + post.getId() + " for subreddit with id " + subreddit.getId());
        return toDto(savedPost);
    }

    @Transactional
    public PostDto updatePost(UUID postId, UpdatePostRequest request) {
        Post post = findPostById(postId);
        User currentUser = getCurrentUser();

        if (post.getDeletionDate() != null) {
            throw new BadRequestException("Deleted posts cannot be edited.");
        }

        if (!post.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Only the post author can edit this post."
            );
        }

        if (request.title() != null) {
            post.setTitle(request.title());
        }

        if (request.content() != null) {
            post.setText(request.content());
        }

        post.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        return toDto(savedPost);
    }
    @Transactional
    public PostDto vote(UUID postId, VoteRequest request) {
        Post post = findPostById(postId);
        User user = userRepository.findByIdAndDeletionDateIsNull(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Optional<PostVote> existingVote = postVoteRepository.findByUserAndPost(user, post);

        if (existingVote.isPresent()) {
            PostVote vote = existingVote.get();
            if (vote.getVoteType() == request.voteType()) {
                // Same vote = remove it (toggle off)
                postVoteRepository.delete(vote);
            } else {
                // Different vote = switch it
                vote.setVoteType(request.voteType());
                postVoteRepository.save(vote);
            }
        } else {
            PostVote newVote = PostVote.builder()
                    .user(user)
                    .post(post)
                    .voteType(request.voteType())
                    .build();
            postVoteRepository.save(newVote);
            logManager.log("Vote post success! User with id " + user.getId() +
                    " voted post with id " + postId);
        }

        return toDto(post);
    }

    @Transactional
    public void deletePost(UUID postId) {
        Post post = findPostById(postId);
        User currentUser = getCurrentUser();

        if (post.getDeletionDate() != null) {
            throw new BadRequestException("Post is already deleted.");
        }

        if (!post.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Only the post author can delete this post."
            );
        }

        post.setTitle("[deleted by user]");
        post.setText(null);
        post.setImage(null);
        post.setDeletionDate(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);

        logManager.log(
                "Delete post success! User with id "
                        + currentUser.getId()
                        + " deleted post with id "
                        + postId
        );
    }

    Post findPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }


    private User getCurrentUser() {
        return userRepository
                .findByUsernameAndDeletionDateIsNull(CURRENT_USERNAME)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + CURRENT_USERNAME
                ));
    }
    private PostDto toDto(Post post) {
        long upvotes = postVoteRepository.countByPostAndVoteType(post, VoteType.UPVOTE);
        long downvotes = postVoteRepository.countByPostAndVoteType(post, VoteType.DOWNVOTE);
        long commentCount = commentRepository.countByPost(post);

        User currentUser = getCurrentUser();

        String userVote = postVoteRepository
                .findByUserAndPost(currentUser, post)
                .map(vote -> {
                    if (vote.getVoteType() == VoteType.UPVOTE) {
                        return "up";
                    }

                    return "down";
                })
                .orElse(null);
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getImage(),
                post.getCreationDate(),
                post.getUpdatedAt(),
                post.getOwner().getId(),
                post.getOwner().getUsername(),
                post.getSubreddit().getId(),
                post.getSubreddit().getName(),
                (int) (upvotes - downvotes),
                (int) upvotes,
                (int) downvotes,
                userVote,
                (int) commentCount
        );
    }
}
