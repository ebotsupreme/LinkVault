package com.linkvault.exception;

public class LinksDeleteException extends RuntimeException {
    public LinksDeleteException(Long userId, Throwable cause) {
        super(String.format(ExceptionMessages.LINKS_DELETE_FAILED, userId), cause);
    }
}
