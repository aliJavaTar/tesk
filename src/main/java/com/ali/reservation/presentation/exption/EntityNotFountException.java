package com.ali.reservation.presentation.exption;

public class EntityNotFountException extends ApplicationException {

    public EntityNotFountException(ErrorType errorType) {
        super(errorType);
    }
}
