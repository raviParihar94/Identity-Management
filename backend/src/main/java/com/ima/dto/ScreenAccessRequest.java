package com.ima.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScreenAccessRequest {
    @NotBlank(message = "Screen ID is required")
    private String screenId;
}
