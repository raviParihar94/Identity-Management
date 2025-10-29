package com.ima.controller;

import com.ima.dto.*;
import com.ima.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Registration successful")
                .data(response)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(response.isMfaRequired() ? "MFA verification required" : "Login successful")
                .data(response)
                .build());
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMfa(
            @Valid @RequestBody MfaVerifyRequest request) {
        AuthResponse response = authenticationService.verifyMfaAndLogin(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("MFA verification successful")
                .data(response)
                .build());
    }
}
