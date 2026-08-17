package com.cnytez.app.e2e;

import com.cnytez.app.dto.request.LoginRequest;
import com.cnytez.app.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthE2ETest extends AbstractE2ETest {

    @Test
    void registerAndLoginFlow() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "user_" + uniqueSuffix;
        String email = "user_" + uniqueSuffix + "@example.com";
        String password = "StrongPassword123!";

        // 1. register user
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Map<String, Object> registerResponse = objectMapper.readValue(registerResult.getResponse().getContentAsString(), Map.class);
        assertThat((Boolean) registerResponse.get("success")).isTrue();

        Map<String, Object> registerData = (Map<String, Object>) registerResponse.get("data");
        assertThat(registerData).containsKey("accessToken");

        // 2. login user
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), Map.class);
        assertThat((Boolean) loginResponse.get("success")).isTrue();

        Map<String, Object> loginData = (Map<String, Object>) loginResponse.get("data");
        assertThat(loginData).containsKey("accessToken");
        assertThat((String) loginData.get("accessToken")).isNotEmpty();
    }

    @Test
    void loginWithInvalidCredentials_fails() throws Exception {
        LoginRequest loginRequest = new LoginRequest("non_existent_user", "wrong_password");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
