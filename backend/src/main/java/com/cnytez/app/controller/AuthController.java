package com.cnytez.app.controller;

import com.cnytez.app.dto.request.*;
import com.cnytez.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cnytez.app.dto.response.ApiResponse;
import com.cnytez.app.dto.response.AuthResponse;
import com.cnytez.app.dto.response.ApiMessageResponse;
import com.cnytez.app.dto.internal.UserProfileDto;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = authService.register(request);
        ApiResponse<AuthResponse> response = new ApiResponse<>(true, auth);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse auth = authService.login(request);
        ApiResponse<AuthResponse> response = new ApiResponse<>(true, auth);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile() {
        UserProfileDto profile = authService.getProfile();
        ApiResponse<UserProfileDto> response =
                new ApiResponse<>(true, profile);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto profile = authService.updateProfile(request);
        ApiResponse<UserProfileDto> response =
                new ApiResponse<>(true, profile);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiMessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(request);

        ApiMessageResponse response = new ApiMessageResponse(
                true,
                "Password changed successfully"
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiMessageResponse> deleteUser(
            @Valid @RequestBody DeleteUserRequest request
    ) {
        authService.deleteUser(request);

        ApiMessageResponse response = new ApiMessageResponse(
                true,
                "Account deleted successfully"
        );

        return ResponseEntity.ok(response);
    }
}
