package cnytez.reddit.app.service;

import cnytez.reddit.app.dto.CommentDto;
import cnytez.reddit.app.dto.CreateCommentRequest;
import cnytez.reddit.app.dto.VoteRequest;
import cnytez.reddit.app.exception.BadRequestException;
import cnytez.reddit.app.exception.ResourceNotFoundException;
import cnytez.reddit.app.log.LogManager;
import cnytez.reddit.app.model.*;
import cnytez.reddit.app.repository.CommentRepository;
import cnytez.reddit.app.repository.CommentVoteRepository;
import cnytez.reddit.app.repository.PostRepository;
import cnytez.reddit.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LogManager logManager;

    public List<CommentDto> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        // Return only top-level comments; clients can fetch replies per comment
        return commentRepository.findByPostAndParentCommentIsNull(post).stream()
                .map(this::toDto)
                .toList();
    }

    public List<CommentDto> getReplies(Long parentCommentId) {
        Comment parent = findCommentById(parentCommentId);
        return commentRepository.findByParentComment(parent).stream()
                .map(this::toDto)
                .toList();
    }

    public List<CommentDto> getCommentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return commentRepository.findByOwner(user).stream()
                .map(this::toDto)
                .toList();
    }

    public CommentDto getCommentById(Long id) {
        return toDto(findCommentById(id));
    }

    @Transactional
    public CommentDto createComment(CreateCommentRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.ownerId()));
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + request.postId()));

        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            parentComment = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent comment not found with id: " + request.parentCommentId()));
            // Ensure parent comment belongs to the same post
            if (!parentComment.getPost().getId().equals(request.postId())) {
                throw new BadRequestException("Parent comment does not belong to the specified post.");
            }
        }

        Comment comment = Comment.builder()
                .title(request.title())
                .text(request.text())
                .image(request.image())
                .owner(owner)
                .post(post)
                .parentComment(parentComment)
                .creationDate(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        CommentVote creatorVote = CommentVote.builder()
                .user(owner)
                .comment(savedComment)
                .voteType(VoteType.UPVOTE)
                .build();
        commentVoteRepository.save(creatorVote);

        logManager.log("Create comment success! User with id " + owner.getId() +
                " created comment with id " + comment.getId());
        return toDto(savedComment);
    }

    @Transactional
    public CommentDto vote(Long commentId, VoteRequest request) {
        Comment comment = findCommentById(commentId);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        Optional<CommentVote> existingVote = commentVoteRepository.findByUserAndComment(user, comment);

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            if (vote.getVoteType() == request.voteType()) {
                // Same vote = remove it (toggle off)
                commentVoteRepository.delete(vote);
            } else {
                // Different vote = switch it
                vote.setVoteType(request.voteType());
                commentVoteRepository.save(vote);
            }
        } else {
            CommentVote newVote = CommentVote.builder()
                    .user(user)
                    .comment(comment)
                    .voteType(request.voteType())
                    .build();
            commentVoteRepository.save(newVote);
        }
        logManager.log("Vote comment success! User with id " + user.getId() +
                " voted comment with id " + commentId);
        return toDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long requestingUserId) {
        Comment comment = findCommentById(commentId);
        if (!comment.getOwner().getId().equals(requestingUserId)) {
            throw new BadRequestException("Only the comment author can delete this comment.");
        }
        commentRepository.deleteById(commentId);
        logManager.log("Delete comment success! User with id " + requestingUserId +
                " deleted comment with id " + commentId);
    }

    private Comment findCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    private CommentDto toDto(Comment comment) {
        long upvotes = commentVoteRepository.countByCommentAndVoteType(comment, VoteType.UPVOTE);
        long downvotes = commentVoteRepository.countByCommentAndVoteType(comment, VoteType.DOWNVOTE);
        return new CommentDto(
                comment.getId(),
                comment.getTitle(),
                comment.getText(),
                comment.getImage(),
                comment.getCreationDate(),
                comment.getOwner().getId(),
                comment.getOwner().getUsername(),
                comment.getPost().getId(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                (int) (upvotes - downvotes),
                (int) upvotes,
                (int) downvotes
        );
    }
}
