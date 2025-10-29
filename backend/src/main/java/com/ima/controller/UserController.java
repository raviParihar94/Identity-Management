package com.ima.controller;

import com.ima.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

public class UserController {
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<String>> userProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("User profile accessed")
                .data("Profile for: " + authentication.getName())
                .build());
    }
}
