package com.ima.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MfaVerifyRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "MFA code is required")
    @Size(min = 6, max = 6, message = "MFA code must be 6 digits")
    private String code;
}
