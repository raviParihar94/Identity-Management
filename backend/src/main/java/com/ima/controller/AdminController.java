package com.ima.controller;

import com.ima.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> adminDashboard() {
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Admin dashboard accessed")
                .data("Welcome to admin dashboard")
                .build());
    }
}
