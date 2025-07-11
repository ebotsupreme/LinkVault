package com.linkvault.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(Long linkId) {
        super(String.format(ExceptionMessages.LINK_NOT_FOUND, linkId));
    }

    public LinkNotFoundException(Long linkId, Throwable cause) {
        super(String.format(ExceptionMessages.LINK_NOT_FOUND, linkId), cause);
    }
}
