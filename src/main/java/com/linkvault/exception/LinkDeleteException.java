package com.linkvault.exception;

import com.linkvault.dto.LinkDto;

public class LinkDeleteException extends RuntimeException {
    public LinkDeleteException(LinkDto linkDto, Throwable cause) {
        super(String.format(ExceptionMessages.LINK_DELETE_FAILED, linkDto.url()), cause);
    }
}
