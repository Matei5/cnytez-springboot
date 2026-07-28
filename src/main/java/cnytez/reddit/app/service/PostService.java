package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.CreatePostRequest;
import cnytez.reddit.app.dto.PostDto;
import cnytez.reddit.app.dto.VoteRequest;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.model.*;
import cnytez.reddit.app.repository.PostRepository;
import cnytez.reddit.app.repository.PostVoteRepository;
import cnytez.reddit.app.repository.SubredditRepository;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final UserRepository userRepository;
    private final SubredditRepository subredditRepository;

    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreationDateDesc().stream()
                .map(this::toDto)
                .toList();
    }

    public List<PostDto> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found with id: " + subredditId));
        return postRepository.findBySubredditOrderByCreationDateDesc(subreddit).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PostDto> getPostsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return postRepository.findByOwner(user).stream()
                .map(this::toDto)
                .toList();
    }

    public PostDto getPostById(Long id) {
        return toDto(findPostById(id));
    }

    @Transactional
    public PostDto createPost(CreatePostRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.ownerId()));
        Subreddit subreddit = subredditRepository.findById(request.subredditId())
                .orElseThrow(() -> new ResourceNotFoundException("Subreddit not found with id: " + request.subredditId()));

        Post post = Post.builder()
                .title(request.title())
                .text(request.text())
                .image(request.image())
                .owner(owner)
                .subreddit(subreddit)
                .creationDate(LocalDateTime.now())
                .build();

        return toDto(postRepository.save(post));
    }

    @Transactional
    public PostDto vote(Long postId, VoteRequest request) {
        Post post = findPostById(postId);
        User user = userRepository.findById(request.userId())
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
        }

        return toDto(post);
    }

    @Transactional
    public void deletePost(Long postId, Long requestingUserId) {
        Post post = findPostById(postId);
        if (!post.getOwner().getId().equals(requestingUserId)) {
            throw new BadRequestException("Only the post author can delete this post.");
        }
        postRepository.deleteById(postId);
    }

    Post findPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    private PostDto toDto(Post post) {
        long upvotes = postVoteRepository.countByPostAndVoteType(post, VoteType.UPVOTE);
        long downvotes = postVoteRepository.countByPostAndVoteType(post, VoteType.DOWNVOTE);
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getImage(),
                post.getCreationDate(),
                post.getOwner().getId(),
                post.getOwner().getUsername(),
                post.getSubreddit().getId(),
                post.getSubreddit().getName(),
                (int) (upvotes - downvotes),
                (int) upvotes,
                (int) downvotes
        );
    }
}
