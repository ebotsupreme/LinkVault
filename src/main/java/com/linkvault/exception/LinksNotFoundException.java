package com.linkvault.exception;

public class LinksNotFoundException extends RuntimeException {
    public LinksNotFoundException(Long userId) {
        super(String.format(ExceptionMessages.LINKS_NOT_FOUND, userId));
    }
}