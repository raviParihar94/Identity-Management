package com.ima.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "error_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "error_code")
    private String errorCode; // e.g., ERR_AUTH_001

    @Column(nullable = false)
    private String message;

    private String description;

    @Column(name = "http_status")
    private int httpStatus;

    @Column(name = "screen_id")
    private String screenId; // Associated screen if applicable
}

