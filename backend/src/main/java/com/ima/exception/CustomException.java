package com.ima.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String errorCode;
    private final String message;

    public CustomException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }
}
