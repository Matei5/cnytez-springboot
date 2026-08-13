package com.cnytez.app.service;

import com.cnytez.app.dto.internal.CommentDto;
import com.cnytez.app.exception.ResourceNotFoundException;
import com.cnytez.app.logging.LogManager;
import com.cnytez.app.mapper.CommentMapper;
import com.cnytez.app.model.Comment;
import com.cnytez.app.repository.CommentRepository;
import com.cnytez.app.repository.CommentVoteRepository;
import com.cnytez.app.repository.PostRepository;
import com.cnytez.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentVoteRepository commentVoteRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LogManager logManager;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getCommentById_Found_ReturnsCommentDto() {
        // arrange
        UUID commentId = UUID.randomUUID();
        Comment comment = Comment.builder().id(commentId).build();
        CommentDto dto = new CommentDto(commentId, null, null, "test", null, 0, 0, 0, null, null, null, null);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentVoteRepository.countByCommentAndVoteType(eq(comment), any())).thenReturn(0L);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        when(commentMapper.toDto(eq(comment), anyLong(), anyLong(), any(), anyList())).thenReturn(dto);

        // act
        CommentDto result = commentService.getCommentById(commentId);

        // assert
        assertNotNull(result);
        assertEquals(commentId, result.id());
    }

    @Test
    void getCommentById_NotFound_ThrowsException() {
        // arrange
        UUID commentId = UUID.randomUUID();
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(commentId));
    }

    @Test
    void getCommentsByPost_ReturnsList() {
        // arrange
        UUID postId = UUID.randomUUID();
        com.cnytez.app.model.Post post = com.cnytez.app.model.Post.builder().id(postId).build();
        Comment comment = Comment.builder().id(UUID.randomUUID()).build();
        CommentDto dto = new CommentDto(comment.getId(), null, null, "test", null, 0, 0, 0, null, null, null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostAndParentCommentIsNull(post)).thenReturn(java.util.List.of(comment));
        when(commentVoteRepository.countByCommentAndVoteType(eq(comment), any())).thenReturn(0L);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        when(commentMapper.toDto(eq(comment), anyLong(), anyLong(), any(), anyList())).thenReturn(dto);

        // act
        java.util.List<CommentDto> result = commentService.getCommentsByPost(postId);

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void countCommentsByPost_ReturnsCount() {
        // arrange
        UUID postId = UUID.randomUUID();
        when(commentRepository.countByPostId(postId)).thenReturn(5L);

        // act
        long count = commentService.countCommentsByPost(postId);

        // assert
        assertEquals(5L, count);
    }

    @Test
    void createComment_success() {
        // arrange
        UUID postId = UUID.randomUUID();
        com.cnytez.app.dto.request.CreateCommentRequest request = new com.cnytez.app.dto.request.CreateCommentRequest("test content", null);
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        com.cnytez.app.model.Post post = com.cnytez.app.model.Post.builder().id(postId).build();
        Comment savedComment = Comment.builder().id(UUID.randomUUID()).text("test content").build();
        CommentDto dto = new CommentDto(savedComment.getId(), null, null, "test content", null, 0, 0, 0, null, null, null, null);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentVoteRepository.countByCommentAndVoteType(eq(savedComment), any())).thenReturn(0L);
        when(commentMapper.toDto(eq(savedComment), anyLong(), anyLong(), any(), anyList())).thenReturn(dto);

        // act
        CommentDto result = commentService.createComment(postId, request);

        // assert
        assertNotNull(result);
        assertEquals("test content", result.content());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_success() {
        // arrange
        UUID commentId = UUID.randomUUID();
        com.cnytez.app.dto.request.UpdateCommentRequest request = new com.cnytez.app.dto.request.UpdateCommentRequest("updated text");
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Comment existingComment = Comment.builder().id(commentId).owner(user).text("old text").build();
        CommentDto dto = new CommentDto(commentId, null, null, "updated text", null, 0, 0, 0, null, null, null, null);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(existingComment);
        when(commentVoteRepository.countByCommentAndVoteType(eq(existingComment), any())).thenReturn(0L);
        when(commentMapper.toDto(eq(existingComment), anyLong(), anyLong(), any(), anyList())).thenReturn(dto);

        // act
        CommentDto result = commentService.updateComment(commentId, request);

        // assert
        assertNotNull(result);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void vote_success() {
        // arrange
        UUID commentId = UUID.randomUUID();
        com.cnytez.app.dto.request.VoteRequest request = new com.cnytez.app.dto.request.VoteRequest("up");
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Comment existingComment = Comment.builder().id(commentId).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));
        when(commentVoteRepository.findByUserAndComment(user, existingComment)).thenReturn(Optional.empty());
        com.cnytez.app.dto.internal.CommentDto mockDto = new com.cnytez.app.dto.internal.CommentDto(commentId, null, null, null, null, 1, 0, 1, null, null, null, null);
        com.cnytez.app.dto.response.VoteResponse mockVoteResponse = new com.cnytez.app.dto.response.VoteResponse(1, 0, 1, "up");

        when(commentMapper.toDto(eq(existingComment), anyLong(), anyLong(), any(), anyList())).thenReturn(mockDto);
        when(commentMapper.toVoteResponse(mockDto)).thenReturn(mockVoteResponse);
        when(commentVoteRepository.countByCommentAndVoteType(existingComment, com.cnytez.app.model.VoteType.UPVOTE)).thenReturn(1L);
        when(commentVoteRepository.countByCommentAndVoteType(existingComment, com.cnytez.app.model.VoteType.DOWNVOTE)).thenReturn(0L);

        // act
        com.cnytez.app.dto.response.VoteResponse result = commentService.vote(commentId, request);

        // assert
        assertNotNull(result);
        assertEquals(1, result.score());
        verify(commentVoteRepository).save(any());
    }

    @Test
    void deleteComment_success() {
        // arrange
        UUID commentId = UUID.randomUUID();
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Comment existingComment = Comment.builder().id(commentId).owner(user).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

        // act
        commentService.deleteComment(commentId);

        // assert
        verify(commentRepository).save(any(Comment.class));
    }
}
