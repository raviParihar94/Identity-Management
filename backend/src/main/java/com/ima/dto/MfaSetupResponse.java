package com.ima.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MfaSetupResponse {
    private String secret;
    private String qrCodeUrl;
    private String manualEntryKey;
}
