package com.cnytez.app.service;

import com.cnytez.app.dto.request.UpdatePostRequest;
import com.cnytez.app.dto.request.CreatePostRequest;
import com.cnytez.app.dto.internal.PostDto;
import com.cnytez.app.dto.request.VoteRequest;
import com.cnytez.app.exception.*;
import com.cnytez.app.log.LogManager;
import com.cnytez.app.mapper.PostMapper;
import com.cnytez.app.model.*;
import com.cnytez.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cnytez.app.dto.response.VoteResponse;

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
    private final PostMapper postMapper;

    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreationDateDesc().stream()
                .map(post ->
                    postMapper.toDto(
                            post,
                            getUpvotes(post),
                            getDownvotes(post),
                            getCommentCount(post),
                            getUserVote(post),
                            getPostFilterId(post)
                    )).toList();
    }

    public List<PostDto> getPostsBySubreddit(String subredditName) {
        Subreddit subreddit = subredditRepository.findByName(subredditName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subreddit not found: " + subredditName
                ));

        return postRepository
                .findBySubredditOrderByCreationDateDesc(subreddit)
                .stream()
                .map(post ->
                        postMapper.toDto(
                                post,
                                getUpvotes(post),
                                getDownvotes(post),
                                getCommentCount(post),
                                getUserVote(post),
                                getPostFilterId(post)
                        )).toList();
    }

    public PostDto getPostById(UUID id) {
        Post post = findPostById(id);
        return postMapper.toDto(
                post,
                getUpvotes(post),
                getDownvotes(post),
                getCommentCount(post),
                getUserVote(post),
                getPostFilterId(post)
        );
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
        return postMapper.toDto(
                savedPost,
                getUpvotes(savedPost),
                getDownvotes(savedPost),
                getCommentCount(savedPost),
                getUserVote(savedPost),
                getPostFilterId(savedPost));
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
        return postMapper.toDto(
                savedPost,
                getUpvotes(savedPost),
                getDownvotes(savedPost),
                getCommentCount(savedPost),
                getUserVote(savedPost),
                getPostFilterId(savedPost));
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
            return postMapper.toVoteResponse(postMapper.toDto(
                    post,
                    getUpvotes(post),
                    getDownvotes(post),
                    getCommentCount(post),
                    getUserVote(post),
                    getPostFilterId(post)
            ));
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

        return postMapper.toVoteResponse(postMapper.toDto(
                post,
                getUpvotes(post),
                getDownvotes(post),
                getCommentCount(post),
                getUserVote(post),
                getPostFilterId(post)
        ));
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

    private long getUpvotes(Post post) {
        return postVoteRepository.countByPostAndVoteType(post, VoteType.UPVOTE);
    }

    private long getDownvotes(Post post) {
        return postVoteRepository.countByPostAndVoteType(post, VoteType.DOWNVOTE);
    }

    private long getCommentCount(Post post) {
        return commentRepository.countByPost(post);
    }

    private String getUserVote(Post post) {
        return currentUserService
                .findCurrentUser()
                .flatMap(user -> postVoteRepository.findByUserAndPost(user, post))
                .map(vote -> {
                    if (vote.getVoteType() == VoteType.UPVOTE) {
                        return "up";
                    }

                    return "down";
                })
                .orElse(null);
    }

    private Integer getPostFilterId(Post post) {
        Filter filter = post.getFilter();
        Integer filterId = null;

        if (filter != null) {
            filterId = filter.getId();
        }

        return filterId;
    }
}
