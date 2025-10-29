package com.ima.exception;


import com.ima.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ERR_AUTH_001")
                .message("Invalid credentials")
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        String details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("ERR_VAL_001")
                .message("Validation failed")
                .details(details)
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder().errorCode("ERR_SYS_001")
                .message("An unexpected error occurred")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now().toString())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode.startsWith("ERR_AUTH")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (errorCode.startsWith("ERR_SCREEN") || errorCode.startsWith("ERR_ACCESS")) {
            return HttpStatus.FORBIDDEN;
        } else if (errorCode.startsWith("ERR_USER") || errorCode.startsWith("ERR_NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        } else if (errorCode.startsWith("ERR_REG") || errorCode.startsWith("ERR_VAL")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}


