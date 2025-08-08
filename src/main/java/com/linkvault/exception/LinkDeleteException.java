package com.linkvault.exception;

public class LinkDeleteException extends RuntimeException {
    public LinkDeleteException(Long linkId, Long userId,  Throwable cause) {
        super(String.format(ExceptionMessages.LINK_DELETE_FAILED, linkId, userId), cause);
    }
}
