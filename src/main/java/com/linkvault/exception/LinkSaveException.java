package com.linkvault.exception;

import com.linkvault.dto.LinkRequest;

public class LinkSaveException extends RuntimeException {
    public LinkSaveException(Long userId, LinkRequest linkRequest, Throwable cause) {
        super(String.format(ExceptionMessages.LINK_SAVE_FAILED, userId, linkRequest.url()), cause);
    }
}
