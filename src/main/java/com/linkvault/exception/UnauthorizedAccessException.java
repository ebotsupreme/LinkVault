package com.linkvault.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String exceptionMessage, Long userId) {
        super(String.format(exceptionMessage, userId));
    }
}
