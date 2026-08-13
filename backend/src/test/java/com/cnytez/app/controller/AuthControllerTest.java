package com.cnytez.app.controller;

import com.cnytez.app.dto.request.LoginRequest;
import com.cnytez.app.dto.request.RegisterRequest;
import com.cnytez.app.dto.request.UpdateProfileRequest;
import com.cnytez.app.dto.request.ChangePasswordRequest;
import com.cnytez.app.dto.request.DeleteUserRequest;
import com.cnytez.app.dto.internal.UserProfileDto;
import com.cnytez.app.dto.response.AuthResponse;
import com.cnytez.app.dto.response.AuthUserDto;
import com.cnytez.app.service.AuthService;
import com.cnytez.app.service.JwtService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for simple controller testing
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.cnytez.app.logging.LogManager logManager;

    @Test
    void register_success() throws Exception {
        // arrange
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        AuthResponse authResponse = new AuthResponse("fake-token", new AuthUserDto("testuser", "test@example.com"));
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // act & assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andExpect(jsonPath("$.data.accessToken").value("fake-token"));
    }

    @Test
    void login_success() throws Exception {
        // arrange
        LoginRequest request = new LoginRequest("testuser", "password123");
        AuthResponse authResponse = new AuthResponse("fake-token", new AuthUserDto("testuser", "test@example.com"));
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // act & assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("testuser"))
                .andExpect(jsonPath("$.data.accessToken").value("fake-token"));
    }

    @Test
    void getProfile_success() throws Exception {
        UserProfileDto profile = new UserProfileDto("testuser", "test@example.com", "Test User", "http://example.com/photo.jpg");
        when(authService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void updateProfile_success() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Updated User", "http://example.com/newphoto.jpg");
        UserProfileDto profile = new UserProfileDto("testuser", "test@example.com", "Updated User", "http://example.com/newphoto.jpg");
        
        when(authService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(profile);

        mockMvc.perform(put("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("Updated User"));
    }

    @Test
    void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldpass", "newpassword123");
        
        doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/auth/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void deleteUser_success() throws Exception {
        DeleteUserRequest request = new DeleteUserRequest("password123");
        
        doNothing().when(authService).deleteUser(any(DeleteUserRequest.class));

        mockMvc.perform(delete("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account deleted successfully"));
    }
}
