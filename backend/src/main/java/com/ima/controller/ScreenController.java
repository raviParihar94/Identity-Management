package com.ima.controller;

import com.ima.dto.ApiResponse;
import com.ima.dto.ScreenAccessRequest;
import com.ima.dto.ScreenAccessResponse;
import com.ima.entity.Screen;
import com.ima.service.ScreenAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
@RequiredArgsConstructor
public class ScreenController {

    private final ScreenAccessService screenAccessService;

    @PostMapping("/check-access")
    public ResponseEntity<ApiResponse<ScreenAccessResponse>> checkAccess(
            @Valid @RequestBody ScreenAccessRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        boolean hasAccess = screenAccessService.hasAccess(username, request.getScreenId());

        ScreenAccessResponse response = ScreenAccessResponse.builder()
                .hasAccess(hasAccess)
                .screenId(request.getScreenId())
                .message(hasAccess ? "Access granted" : "Access denied")
                .build();

        return ResponseEntity.ok(ApiResponse.<ScreenAccessResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/accessible")
    public ResponseEntity<ApiResponse<List<Screen>>> getAccessibleScreens(
            Authentication authentication) {

        String username = authentication.getName();
        List<Screen> screens = screenAccessService.getAccessibleScreens(username);

        return ResponseEntity.ok(ApiResponse.<List<Screen>>builder()
                .success(true)
                .message("Accessible screens retrieved")
                .data(screens)
                .build());
    }
}


