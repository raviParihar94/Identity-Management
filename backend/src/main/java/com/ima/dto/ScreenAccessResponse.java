package com.ima.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenAccessResponse {
    private boolean hasAccess;
    private String screenId;
    private String screenName;
    private String message;
}
