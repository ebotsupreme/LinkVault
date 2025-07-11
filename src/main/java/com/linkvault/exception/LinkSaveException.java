package com.linkvault.exception;

import com.linkvault.dto.LinkDto;

public class LinkSaveException extends RuntimeException {
    public LinkSaveException(LinkDto linkDto, Throwable cause) {
        super(String.format(ExceptionMessages.LINK_SAVE_FAILED, linkDto.url()), cause);
    }
}
