package com.linkvault.exception;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException(String username) {
        super("Password must be at least 8 characters for user: " + username);
    }
}
