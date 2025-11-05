package com.ima.controller;

import com.ima.dto.*;
import com.ima.security.JwtService;
import com.ima.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // ============================================================
    // 1️⃣ Register user
    // ============================================================
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

    // ============================================================
    // 2️⃣ Standard username/password login
    // ============================================================
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

    // ============================================================
    // 3️⃣ MFA verification endpoint
    // ============================================================
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

    // ============================================================
    // 4️⃣ OAuth or JWT Auto Refresh
    // ============================================================
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String oldTokenHeader,
            @RequestAttribute(value = "expiredToken", required = false) String expiredToken,
            @RequestAttribute(value = "username", required = false) String usernameAttr) {

        String tokenToUse = expiredToken;
        String username = usernameAttr;

        // Fallback if request came directly with old JWT
        if ((tokenToUse == null || username == null) && oldTokenHeader != null && oldTokenHeader.startsWith("Bearer ")) {
            tokenToUse = oldTokenHeader.substring(7);
            username = jwtService.extractUsername(tokenToUse);
        }

        if (username == null)
            return ResponseEntity.badRequest().body(ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("Invalid or missing token")
                    .build());

        UserDetails user = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .success(true)
                .message("Tokens refreshed successfully")
                .data(Map.of(
                        "accessToken", newAccessToken,
                        "refreshToken", newRefreshToken
                ))
                .build());
    }
}
