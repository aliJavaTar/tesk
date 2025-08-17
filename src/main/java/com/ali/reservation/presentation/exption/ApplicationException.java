package com.ali.reservation.presentation.exption;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorType errorType;
    private final String details;

    public ApplicationException(ErrorType errorType, String details) {
        super(details != null ? errorType.getDefaultMessage() + ": " + details : errorType.getDefaultMessage());
        this.errorType = errorType;
        this.details = details;
    }

    public ApplicationException(ErrorType errorType) {
        super(errorType.getDefaultMessage());
        this.errorType = errorType;
        this.details = null;
    }

    public HttpStatus getStatus() {
        return errorType.getHttpStatus();
    }
}