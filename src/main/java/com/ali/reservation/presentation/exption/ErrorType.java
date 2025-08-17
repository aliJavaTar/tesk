package com.ali.reservation.presentation.exption;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorType {
    // Authentication & Authorization errors (1xx)
    INVALID_CREDENTIALS(101, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(102, "You do not have permission to access this resource", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(103, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_EXISTS(104, "Username already exists", HttpStatus.CONFLICT),
    UNAUTHORIZED(105, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    // Token errors (2xx)
    TOKEN_EXPIRED(201, "Token has expired", HttpStatus.FORBIDDEN),
    TOKEN_INVALID(202, "Token is invalid", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID(203, "Refresh token is invalid", HttpStatus.FORBIDDEN),

    // Validation errors (3xx)
    VALIDATION_ERROR(301, "Input validation failed", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION(302, "Parameter validation failed", HttpStatus.BAD_REQUEST),
    INVALID_OTP(303, "Invalid or expired OTP", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER(304, "Invalid phone number", HttpStatus.BAD_REQUEST),

    CONFLICT(306, "CONFLICT DATA", HttpStatus.BAD_REQUEST),

    DUPLICATE_ERROR(305, "already exist", HttpStatus.BAD_REQUEST),
    // Business logic errors (4xx)
    ENTITY_NOT_FOUND(402, "Entity not found", HttpStatus.NOT_FOUND),

    // System errors (5xx)
    INTERNAL_ERROR(501, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int errorCode;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorType(int errorCode, String defaultMessage, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}