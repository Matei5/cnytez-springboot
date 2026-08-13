package com.cnytez.app.service;

import com.cnytez.app.dto.internal.PostDto;
import com.cnytez.app.exception.ResourceNotFoundException;
import com.cnytez.app.logging.LogManager;
import com.cnytez.app.mapper.PostMapper;
import com.cnytez.app.model.Post;
import com.cnytez.app.repository.CommentRepository;
import com.cnytez.app.repository.FilterRepository;
import com.cnytez.app.repository.PostRepository;
import com.cnytez.app.repository.PostVoteRepository;
import com.cnytez.app.repository.SubredditRepository;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostVoteRepository postVoteRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private SubredditRepository subredditRepository;
    @Mock
    private FilterRepository filterRepository;
    @Mock
    private LogManager logManager;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ImageUploadService imageUploadService;
    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void getPostById_Found_ReturnsPostDto() {
        // arrange
        UUID postId = UUID.randomUUID();
        Post post = Post.builder().id(postId).build();
        PostDto dto = new PostDto(postId, "title", "content", null, null, null, null, 0, 0, 0, 0, null, null, null);
        
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postVoteRepository.countByPostAndVoteType(eq(post), any())).thenReturn(0L);
        when(commentRepository.countByPost(post)).thenReturn(0L);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        when(postMapper.toDto(eq(post), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(dto);

        // act
        PostDto result = postService.getPostById(postId);

        // assert
        assertNotNull(result);
        assertEquals(postId, result.id());
    }

    @Test
    void getPostById_NotFound_ThrowsException() {
        // arrange
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(postId));
    }

    @Test
    void getAllPosts_ReturnsList() {
        // arrange
        Post post = Post.builder().id(UUID.randomUUID()).build();
        PostDto dto = new PostDto(post.getId(), "title", "content", null, null, null, null, 0, 0, 0, 0, null, null, null);
        
        when(postRepository.findAllByOrderByCreationDateDesc()).thenReturn(java.util.List.of(post));
        when(postVoteRepository.countByPostAndVoteType(eq(post), any())).thenReturn(0L);
        when(commentRepository.countByPost(post)).thenReturn(0L);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        when(postMapper.toDto(eq(post), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(dto);

        // act
        java.util.List<PostDto> result = postService.getAllPosts();

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getPostsBySubreddit_ReturnsList() {
        // arrange
        com.cnytez.app.model.Subreddit subreddit = com.cnytez.app.model.Subreddit.builder().name("testsub").build();
        Post post = Post.builder().id(UUID.randomUUID()).build();
        PostDto dto = new PostDto(post.getId(), "title", "content", null, null, null, null, 0, 0, 0, 0, null, null, null);
        
        when(subredditRepository.findByName("testsub")).thenReturn(Optional.of(subreddit));
        when(postRepository.findBySubredditOrderByCreationDateDesc(subreddit)).thenReturn(java.util.List.of(post));
        when(postVoteRepository.countByPostAndVoteType(eq(post), any())).thenReturn(0L);
        when(commentRepository.countByPost(post)).thenReturn(0L);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        when(postMapper.toDto(eq(post), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(dto);

        // act
        java.util.List<PostDto> result = postService.getPostsBySubreddit("testsub");

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllPostsOrGetPostsBySubreddit_ReturnsList() {
        // arrange
        Post post = Post.builder().id(UUID.randomUUID()).build();
        PostDto dto = new PostDto(post.getId(), "title", "content", null, null, null, null, 0, 0, 0, 0, null, null, null);
        
        when(postRepository.findAllByOrderByCreationDateDesc()).thenReturn(java.util.List.of(post));
        when(postVoteRepository.countByPostAndVoteType(eq(post), any())).thenReturn(0L);
        when(commentRepository.countByPost(post)).thenReturn(0L);
        when(currentUserService.findCurrentUser()).thenReturn(Optional.empty());
        when(postMapper.toDto(eq(post), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(dto);

        // act
        java.util.List<PostDto> result = postService.getAllPostsOrGetPostsBySubreddit("");

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createPost_success() {
        // arrange
        com.cnytez.app.dto.request.CreatePostRequest request = new com.cnytez.app.dto.request.CreatePostRequest("Title", "Content", "testsub", null, null);
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        com.cnytez.app.model.Subreddit subreddit = com.cnytez.app.model.Subreddit.builder().id(UUID.randomUUID()).name("testsub").build();
        Post savedPost = Post.builder().id(UUID.randomUUID()).title("Title").build();
        PostDto dto = new PostDto(savedPost.getId(), "Title", "Content", null, null, null, null, 0, 0, 0, 0, null, null, null);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(subredditRepository.findByName("testsub")).thenReturn(Optional.of(subreddit));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(postVoteRepository.countByPostAndVoteType(eq(savedPost), any())).thenReturn(0L);
        when(commentRepository.countByPost(savedPost)).thenReturn(0L);
        when(postMapper.toDto(eq(savedPost), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(dto);

        // act
        PostDto result = postService.createPost(request);

        // assert
        assertNotNull(result);
        assertEquals("Title", result.title());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_success() {
        // arrange
        UUID postId = UUID.randomUUID();
        com.cnytez.app.dto.request.UpdatePostRequest request = new com.cnytez.app.dto.request.UpdatePostRequest("New Title", "New Content");
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Post existingPost = Post.builder().id(postId).owner(user).title("Old Title").build();
        PostDto dto = new PostDto(postId, "New Title", "New Content", null, null, null, null, 0, 0, 0, 0, null, null, null);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);
        when(postVoteRepository.countByPostAndVoteType(eq(existingPost), any())).thenReturn(0L);
        when(commentRepository.countByPost(existingPost)).thenReturn(0L);
        when(postMapper.toDto(eq(existingPost), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(dto);

        // act
        PostDto result = postService.updatePost(postId, request);

        // assert
        assertNotNull(result);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void vote_success() {
        // arrange
        UUID postId = UUID.randomUUID();
        com.cnytez.app.dto.request.VoteRequest request = new com.cnytez.app.dto.request.VoteRequest("up");
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Post existingPost = Post.builder().id(postId).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postVoteRepository.findByUserAndPost(user, existingPost)).thenReturn(Optional.empty());
        com.cnytez.app.dto.internal.PostDto mockDto = new com.cnytez.app.dto.internal.PostDto(postId, null, null, null, null, null, null, 1, 0, 1, 0, null, null, null);
        com.cnytez.app.dto.response.VoteResponse mockVoteResponse = new com.cnytez.app.dto.response.VoteResponse(1, 0, 1, "up");

        when(postMapper.toDto(eq(existingPost), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(mockDto);
        when(postMapper.toVoteResponse(mockDto)).thenReturn(mockVoteResponse);
        when(postVoteRepository.countByPostAndVoteType(existingPost, com.cnytez.app.model.VoteType.UPVOTE)).thenReturn(1L);
        when(postVoteRepository.countByPostAndVoteType(existingPost, com.cnytez.app.model.VoteType.DOWNVOTE)).thenReturn(0L);

        // act
        com.cnytez.app.dto.response.VoteResponse result = postService.vote(postId, request);

        // assert
        assertNotNull(result);
        assertEquals(1, result.score());
        verify(postVoteRepository).save(any());
    }

    @Test
    void deletePost_success() {
        // arrange
        UUID postId = UUID.randomUUID();
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Post existingPost = Post.builder().id(postId).owner(user).build();

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // act
        postService.deletePost(postId);

        // assert
        verify(postRepository).save(any(Post.class));
    }
}
