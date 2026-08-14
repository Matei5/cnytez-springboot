package com.cnytez.app.service;

import com.cnytez.app.dto.internal.SubredditDto;
import com.cnytez.app.exception.ResourceNotFoundException;
import com.cnytez.app.logging.LogManager;
import com.cnytez.app.mapper.SubredditMapper;
import com.cnytez.app.model.Subreddit;
import com.cnytez.app.repository.PostRepository;
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
class SubredditServiceTest {

    @Mock
    private SubredditRepository subredditRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private LogManager logManager;
    @Mock
    private SubredditMapper subredditMapper;

    @InjectMocks
    private SubredditService subredditService;

    @Test
    void getSubredditByName_Found_ReturnsSubredditDto() {
        // arrange
        String name = "testsub";
        Subreddit subreddit = Subreddit.builder().id(UUID.randomUUID()).name(name).build();
        SubredditDto dto = new SubredditDto(subreddit.getId(), name, "Test Sub", null, 0, 0L, null, null);
        
        when(subredditRepository.findByName(name)).thenReturn(Optional.of(subreddit));
        when(postRepository.countBySubreddit(subreddit)).thenReturn(0L);
        when(subredditMapper.toDto(eq(subreddit), anyLong())).thenReturn(dto);

        // act
        SubredditDto result = subredditService.getSubredditByName(name);

        // assert
        assertNotNull(result);
        assertEquals(name, result.name());
    }

    @Test
    void getSubredditByName_NotFound_ThrowsException() {
        // arrange
        String name = "testsub";
        when(subredditRepository.findByName(name)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(ResourceNotFoundException.class, () -> subredditService.getSubredditByName(name));
    }

    @Test
    void getAllSubreddits_ReturnsList() {
        // arrange
        Subreddit subreddit = Subreddit.builder().id(UUID.randomUUID()).name("testsub").build();
        com.cnytez.app.dto.internal.SubredditDto dto = new com.cnytez.app.dto.internal.SubredditDto(subreddit.getId(), "testsub", "Test Sub", null, 0, 0L, null, null);
        
        when(subredditRepository.findAll()).thenReturn(java.util.List.of(subreddit));
        when(postRepository.countBySubreddit(subreddit)).thenReturn(0L);
        when(subredditMapper.toDto(eq(subreddit), anyLong())).thenReturn(dto);

        // act
        java.util.List<com.cnytez.app.dto.internal.SubredditDto> result = subredditService.getAllSubreddits();

        // assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testsub", result.get(0).name());
    }

    @Test
    void createSubreddit_success() {
        // arrange
        com.cnytez.app.dto.request.CreateSubredditRequest request = new com.cnytez.app.dto.request.CreateSubredditRequest("newsub", "Display Name", "description", null);
        Subreddit saved = Subreddit.builder().id(UUID.randomUUID()).name("newsub").build();
        com.cnytez.app.dto.internal.SubredditDto dto = new com.cnytez.app.dto.internal.SubredditDto(saved.getId(), "newsub", "description", null, 0, 0L, null, null);
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();

        when(subredditRepository.existsByName("newsub")).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(subredditRepository.save(any(Subreddit.class))).thenReturn(saved);
        when(subredditMapper.toDto(eq(saved), anyLong())).thenReturn(dto);

        // act
        com.cnytez.app.dto.internal.SubredditDto result = subredditService.createSubreddit(request);

        // assert
        assertNotNull(result);
        assertEquals("newsub", result.name());
        verify(subredditRepository).save(any(Subreddit.class));
    }

    @Test
    void createSubreddit_nameTaken_throwsConflictException() {
        // arrange
        com.cnytez.app.dto.request.CreateSubredditRequest request = new com.cnytez.app.dto.request.CreateSubredditRequest("newsub", "Display Name", "description", null);
        when(subredditRepository.existsByName("newsub")).thenReturn(true);

        // act & assert
        assertThrows(
                com.cnytez.app.exception.ConflictException.class,
                () -> subredditService.createSubreddit(request)
        );
        verify(subredditRepository, never()).save(any(Subreddit.class));
    }

    @Test
    void updateSubreddit_success() {
        // arrange
        com.cnytez.app.dto.request.UpdateSubredditRequest request = new com.cnytez.app.dto.request.UpdateSubredditRequest("new description", "http://icon.png", "http://banner.png");
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Subreddit existing = Subreddit.builder().id(UUID.randomUUID()).name("testsub").owner(user).build();
        com.cnytez.app.dto.internal.SubredditDto dto = new com.cnytez.app.dto.internal.SubredditDto(existing.getId(), "testsub", "new description", null, 0, 0L, null, null);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(subredditRepository.findByName("testsub")).thenReturn(Optional.of(existing));
        when(subredditRepository.save(any(Subreddit.class))).thenReturn(existing);
        when(postRepository.countBySubreddit(existing)).thenReturn(0L);
        when(subredditMapper.toDto(eq(existing), anyLong())).thenReturn(dto);

        // act
        com.cnytez.app.dto.internal.SubredditDto result = subredditService.updateSubreddit("testsub", request);

        // assert
        assertNotNull(result);
        verify(subredditRepository).save(any(Subreddit.class));
    }

    @Test
    void deleteSubreddit_success() {
        // arrange
        com.cnytez.app.model.User user = com.cnytez.app.model.User.builder().id(UUID.randomUUID()).username("testuser").build();
        Subreddit existing = Subreddit.builder().id(UUID.randomUUID()).name("testsub").owner(user).build();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(subredditRepository.findByName("testsub")).thenReturn(Optional.of(existing));

        // act
        subredditService.deleteSubreddit("testsub");

        // assert
        verify(subredditRepository).delete(existing);
    }
}
