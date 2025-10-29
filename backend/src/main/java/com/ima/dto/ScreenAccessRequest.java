package com.ima.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenAccessRequest {
    @NotBlank(message = "Screen ID is required")
    private String screenId;
}
