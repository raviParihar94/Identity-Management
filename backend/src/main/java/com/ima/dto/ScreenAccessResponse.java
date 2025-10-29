package com.ima.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScreenAccessResponse {
    private boolean hasAccess;
    private String screenId;
    private String screenName;
    private String message;
}
