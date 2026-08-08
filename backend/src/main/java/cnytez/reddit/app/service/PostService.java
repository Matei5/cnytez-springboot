package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.request.UpdatePostRequest;
import cnytez.reddit.app.dto.request.CreatePostRequest;
import cnytez.reddit.app.dto.internal.PostDto;
import cnytez.reddit.app.dto.request.VoteRequest;
import cnytez.reddit.app.exception.*;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.*;
import cnytez.reddit.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cnytez.reddit.app.dto.response.VoteResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentRepository commentRepository;
    private final SubredditRepository subredditRepository;
    private final FilterRepository filterRepository;

    private final LogManager logManager;
    private final CurrentUserService currentUserService;
    private final ImageUploadService imageUploadService;

    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreationDateDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public List<PostDto> getPostsBySubreddit(String subredditName) {
        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subreddit not found: " + subredditName
                ));

        return postRepository
                .findBySubredditOrderByCreationDateDesc(subreddit)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PostDto getPostById(UUID id) {
        return toDto(findPostById(id));
    }

    @Transactional
    public PostDto createPost(CreatePostRequest request) {
        User owner = currentUserService.getCurrentUser();
        Subreddit subreddit = subredditRepository.findByName(request.subreddit())
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found with name: " + request.subreddit()));

        String imageUrl = null;
        Filter filter = null;
        if (request.image() != null) {
            filter = filterRepository.findById(request.filter()).orElseThrow(
                    () -> new BadRequestException("Invalid filter id: " + request.filter())
            );

            try {
                imageUrl = imageUploadService.sendImageToServer(request.image(), filter.getName());
            } catch (IllegalArgumentException | RejectedFileException e) {
                throw new BadRequestException("Failed to process the image: " + e.getMessage());
            } catch (ImageServerFailureException e) {
                throw new InternalServerErrorException("Failed to process the image: " + e.getMessage());
            }
        }

        Post post = Post.builder()
                .title(request.title())
                .text(request.content())
                .image(imageUrl)
                .filter(filter)
                .owner(owner)
                .subreddit(subreddit)
                .creationDate(Instant.now())
                .updatedAt(null)
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
        User currentUser = currentUserService.getCurrentUser();

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

        post.setUpdatedAt(Instant.now());

        Post savedPost = postRepository.save(post);
        return toDto(savedPost);
    }
    @Transactional
    public VoteResponse vote(UUID postId, VoteRequest request) {
        Post post = findPostById(postId);
        User currentUser = currentUserService.getCurrentUser();

        if (post.getDeletionDate() != null) {
            throw new BadRequestException("Deleted posts cannot be voted.");
        }

        Optional<PostVote> existingVote =
                postVoteRepository.findByUserAndPost(currentUser, post);

        if ("none".equalsIgnoreCase(request.voteType())) {
            existingVote.ifPresent(postVoteRepository::delete);
            return toVoteResponse(post);
        }

        VoteType newVoteType;

        if ("up".equalsIgnoreCase(request.voteType())) {
            newVoteType = VoteType.UPVOTE;
        } else if ("down".equalsIgnoreCase(request.voteType())) {
            newVoteType = VoteType.DOWNVOTE;
        } else {
            throw new BadRequestException(
                    "Vote type must be up, down or none."
            );
        }

        if (existingVote.isPresent()) {
            PostVote vote = existingVote.get();
            vote.setVoteType(newVoteType);
            postVoteRepository.save(vote);
        } else {
            PostVote newVote = PostVote.builder()
                    .user(currentUser)
                    .post(post)
                    .voteType(newVoteType)
                    .build();

            postVoteRepository.save(newVote);
        }

        return toVoteResponse(post);
    }

    @Transactional
    public void deletePost(UUID postId) {
        Post post = findPostById(postId);
        User currentUser = currentUserService.getCurrentUser();

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
        Instant now = Instant.now();
        post.setDeletionDate(now);
        post.setUpdatedAt(now);

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


    private VoteResponse toVoteResponse(Post post) {
        PostDto postDto = toDto(post);

        return new VoteResponse(
                postDto.upvotes(),
                postDto.downvotes(),
                postDto.score(),
                postDto.userVote()
        );
    }
    private PostDto toDto(Post post) {
        long upvotes = postVoteRepository.countByPostAndVoteType(post, VoteType.UPVOTE);
        long downvotes = postVoteRepository.countByPostAndVoteType(post, VoteType.DOWNVOTE);
        long commentCount = commentRepository.countByPost(post);

        String userVote = currentUserService
                .findCurrentUser()
                .flatMap(user -> postVoteRepository.findByUserAndPost(user, post))
                .map(vote -> {
                    if (vote.getVoteType() == VoteType.UPVOTE) {
                        return "up";
                    }

                    return "down";
                })
                .orElse(null);

        Filter filter = post.getFilter();
        Integer filterId = null;

        if (filter != null) {
            filterId = filter.getId();
        }

        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getImage(),
                filterId,
                post.getOwner().getUsername(),
                post.getSubreddit().getName(),
                (int) upvotes,
                (int) downvotes,
                (int) (upvotes - downvotes),
                (int) commentCount,
                userVote,
                post.getCreationDate(),
                post.getUpdatedAt()
        );
    }
}
