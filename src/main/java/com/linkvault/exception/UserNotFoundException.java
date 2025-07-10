package com.linkvault.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(Long userId) {
        super(String.format(ExceptionMessages.USER_NOT_FOUND, userId));
    }
}
