package com.cnytez.app.e2e;

import com.cnytez.app.dto.request.ChangePasswordRequest;
import com.cnytez.app.dto.request.DeleteUserRequest;
import com.cnytez.app.dto.request.LoginRequest;
import com.cnytez.app.dto.request.RegisterRequest;
import com.cnytez.app.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileE2ETest extends AbstractE2ETest {

    @Test
    void userProfileAndCredentialsLifecycleFlow() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "profile_" + uniqueSuffix;
        String email = "profile_" + uniqueSuffix + "@example.com";
        String initialPassword = "InitialPassword123!";
        String newPassword = "UpdatedPassword123!";

        // 1. register
        RegisterRequest registerRequest = new RegisterRequest(username, email, initialPassword);
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> registerResponse = objectMapper.readValue(registerResult.getResponse().getContentAsString(), Map.class);
        String token = (String) ((Map<String, Object>) registerResponse.get("data")).get("accessToken");

        // 2. update profile
        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(
                "My Display Name",
                "https://example.com/avatar.png"
        );
        mockMvc.perform(put("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("My Display Name"))
                .andExpect(jsonPath("$.data.avatarUrl").value("https://example.com/avatar.png"));

        // 3. verify profile via get /auth/me
        mockMvc.perform(get("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.displayName").value("My Display Name"));

        // 4. change password
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(initialPassword, newPassword);
        mockMvc.perform(put("/auth/me/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. attempt login with old password: 401 unauthorized
        LoginRequest oldLoginRequest = new LoginRequest(username, initialPassword);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLoginRequest)))
                .andExpect(status().isUnauthorized());

        // 6. login with new password: 200 ok
        LoginRequest newLoginRequest = new LoginRequest(username, newPassword);
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        String freshToken = (String) ((Map<String, Object>) loginResponse.get("data")).get("accessToken");
        assertThat(freshToken).isNotEmpty();

        // 7. delete account
        DeleteUserRequest deleteRequest = new DeleteUserRequest(newPassword);
        mockMvc.perform(delete("/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + freshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 8. subsequent login attempt fails: 401 unauthorized
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isUnauthorized());

        // 9. attempting to recreate an account with the same username fails with 409 conflict
        RegisterRequest sameUsernameRequest = new RegisterRequest(username, "new_" + email, "BrandNewPassword123!");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sameUsernameRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 10. recreating an account with a new username but the same email succeeds (email is reusable)
        RegisterRequest reuseEmailRequest = new RegisterRequest("new_" + username, email, "BrandNewPassword123!");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reuseEmailRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}
