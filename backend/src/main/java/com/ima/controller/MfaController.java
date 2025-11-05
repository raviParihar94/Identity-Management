package com.ima.controller;

import com.ima.dto.ApiResponse;
import com.ima.dto.MfaSetupResponse;
import com.ima.service.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(Authentication authentication) {
        String username = authentication.getName();
        String secret = mfaService.generateSecretKey();
        String qrCodeUrl = mfaService.getQRCodeUrl(username, secret);

        MfaSetupResponse response = MfaSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .manualEntryKey(secret)
                .build();

        return ResponseEntity.ok(ApiResponse.<MfaSetupResponse>builder()
                .success(true)
                .message("MFA setup initiated")
                .data(response)
                .build());
    }

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<String>> enableMfa(
            @RequestParam String secret,
            @RequestParam String code,
            Authentication authentication) {

        boolean isValid = mfaService.verifyCode(secret, code);

        if (!isValid) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Invalid MFA code")
                    .errorCode("ERR_MFA_002")
                    .build());
        }

        mfaService.enableMfa(authentication.getName(), secret);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("MFA enabled successfully")
                .build());
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<String>> disableMfa(Authentication authentication) {
        mfaService.disableMfa(authentication.getName());

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("MFA disabled successfully")
                .build());
    }
}

