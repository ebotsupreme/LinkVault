package com.linkvault.exception;

public class LinkSaveException extends RuntimeException {
    public LinkSaveException(Long linkId, Long userId, Throwable cause) {
        super(String.format(ExceptionMessages.LINK_SAVE_FAILED, linkId, userId), cause);
    }
}
