package com.linkvault.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
