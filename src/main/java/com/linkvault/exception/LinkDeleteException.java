package com.linkvault.exception;

import com.linkvault.dto.LinkRequest;

public class LinkDeleteException extends RuntimeException {
    public LinkDeleteException(Long linkId, Long userId,  Throwable cause) {
        super(String.format(ExceptionMessages.LINK_DELETE_FAILED, linkId, userId), cause);
    }
}
