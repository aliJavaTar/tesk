package com.ali.reservation.presentation.exption;

public class TimeNotValidException extends ApplicationException {
    public TimeNotValidException(ErrorType errorType, String details) {
        super(errorType, details);
    }
}
