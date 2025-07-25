package com.linkvault.exception;

public class RegistrationFailedException extends RuntimeException {
    public RegistrationFailedException(String username, Throwable cause) {
        super("Unexpected error during registration for user: " + username, cause);
    }
}
