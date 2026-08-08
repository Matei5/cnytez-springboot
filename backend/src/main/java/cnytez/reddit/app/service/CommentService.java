package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.*;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.mapper.CommentMapper;
import cnytez.reddit.app.model.*;
import cnytez.reddit.app.repository.CommentRepository;
import cnytez.reddit.app.repository.CommentVoteRepository;
import cnytez.reddit.app.repository.PostRepository;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LogManager logManager;
    private final CurrentUserService currentUserService;
    private final CommentMapper commentMapper;

    public List<CommentDto> getCommentsByPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        // Return only top-level comments; clients can fetch replies per comment
        return commentRepository.findByPostAndParentCommentIsNull(post).stream()
                .map(comment ->
                        commentMapper.toDto(
                                comment,
                                getUpvotes(comment),
                                getDownvotes(comment),
                                getUserVote(comment),
                                getReplies(comment)
                        )).toList();
    }



    public CommentDto getCommentById(UUID id) {
        Comment comment = findCommentById(id);
        return commentMapper.toDto(
                comment,
                getUpvotes(comment),
                getDownvotes(comment),
                getUserVote(comment),
                getReplies(comment)
                );
    }

    @Transactional
    public CommentDto createComment(
            UUID postId,
            CreateCommentRequest request
    ) {
        User owner = currentUserService.getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post not found with id: " + postId
                ));

        if (post.getDeletionDate() != null) {
            throw new BadRequestException(
                    "Comments cannot be added to a deleted post."
            );
        }

        Comment parentComment = null;

        if (request.parentId() != null) {
            parentComment = findCommentById(request.parentId());

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new BadRequestException(
                        "Parent comment does not belong to this post."
                );
            }

            if (parentComment.getDeletionDate() != null) {
                throw new BadRequestException(
                        "Replies cannot be added to a deleted comment."
                );
            }
        }

        Instant now = Instant.now();

        Comment comment = Comment.builder()
                .text(request.content())
                .owner(owner)
                .post(post)
                .parentComment(parentComment)
                .creationDate(now)
                .updatedAt(null)
                .build();

        Comment savedComment = commentRepository.save(comment);

        CommentVote creatorVote = CommentVote.builder()
                .user(owner)
                .comment(savedComment)
                .voteType(VoteType.UPVOTE)
                .build();

        commentVoteRepository.save(creatorVote);

        logManager.log(
                "Create comment success! User with id "
                        + owner.getId()
                        + " created comment with id "
                        + savedComment.getId()
        );

        return commentMapper.toDto(
                savedComment,
                getUpvotes(savedComment),
                getDownvotes(savedComment),
                getUserVote(savedComment),
                getReplies(savedComment)
        );
    }
    @Transactional
    public VoteResponse vote(UUID commentId, VoteRequest request) {
        Comment comment = findCommentById(commentId);
        User currentUser = currentUserService.getCurrentUser();

        if (comment.getDeletionDate() != null) {
            throw new BadRequestException("Deleted comments cannot be voted.");
        }

        Optional<CommentVote> existingVote =
                commentVoteRepository.findByUserAndComment(currentUser, comment);

        if ("none".equalsIgnoreCase(request.voteType())) {
            existingVote.ifPresent(commentVoteRepository::delete);
            return commentMapper.toVoteResponse(
                    commentMapper.toDto(
                            comment,
                            getUpvotes(comment),
                            getDownvotes(comment),
                            getUserVote(comment),
                            getReplies(comment)
                    )
            );
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
            CommentVote vote = existingVote.get();
            vote.setVoteType(newVoteType);
            commentVoteRepository.save(vote);
        } else {
            CommentVote newVote = CommentVote.builder()
                    .user(currentUser)
                    .comment(comment)
                    .voteType(newVoteType)
                    .build();

            commentVoteRepository.save(newVote);
        }

        return commentMapper.toVoteResponse(commentMapper.toDto(
                comment,
                getUpvotes(comment),
                getDownvotes(comment),
                getUserVote(comment),
                getReplies(comment)
        ));
    }
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = findCommentById(commentId);
        User currentUser = currentUserService.getCurrentUser();

        if (comment.getDeletionDate() != null) {
            throw new BadRequestException(
                    "Comment is already deleted."
            );
        }

        if (!comment.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Only the comment author can delete this comment."
            );
        }

        Instant now = Instant.now();

        comment.setText("[deleted by user]");
        comment.setDeletionDate(now);
        comment.setUpdatedAt(now);

        commentRepository.save(comment);

        logManager.log(
                "Delete comment success! User with id "
                        + currentUser.getId()
                        + " deleted comment with id "
                        + commentId
        );
    }

    @Transactional
    public CommentDto updateComment(
            UUID commentId,
            UpdateCommentRequest request
    ) {
        Comment comment = findCommentById(commentId);
        User currentUser = currentUserService.getCurrentUser();

        if (comment.getDeletionDate() != null) {
            throw new BadRequestException(
                    "Deleted comments cannot be edited."
            );
        }

        if (!comment.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Only the comment author can edit this comment."
            );
        }

        comment.setText(request.content());
        comment.setUpdatedAt(Instant.now());

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(
                savedComment,
                getUpvotes(savedComment),
                getDownvotes(savedComment),
                getUserVote(savedComment),
                getReplies(savedComment));
    }

    private Comment findCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    public long countCommentsByPost(UUID postId) {
        return commentRepository.countByPost_Id(postId);
    }

    private long getUpvotes(Comment comment) {
        return commentVoteRepository.countByCommentAndVoteType(comment, VoteType.UPVOTE);
    }

    private long getDownvotes(Comment comment) {
        return commentVoteRepository.countByCommentAndVoteType(comment, VoteType.DOWNVOTE);
    }

    private String getUserVote(Comment comment) {
        return currentUserService
                .findCurrentUser()
                .flatMap(user ->
                        commentVoteRepository.findByUserAndComment(user, comment)
                )
                .map(vote -> {
                    if (vote.getVoteType() == VoteType.UPVOTE) {
                        return "up";
                    }

                    return "down";
                })
                .orElse(null);
    }

    private List<CommentDto> getReplies(Comment comment) {
        return commentRepository
                .findByParentComment(comment)
                .stream()
                .map(reply ->
                        commentMapper.toDto(
                                reply,
                                getUpvotes(reply),
                                getDownvotes(reply),
                                getUserVote(reply),
                                getReplies(reply)
                        )).toList();
    }
}
