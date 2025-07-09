package com.linkvault.exception;

import com.linkvault.dto.LinkDto;

public class LinkSaveException extends RuntimeException {
    public LinkSaveException(LinkDto linkDto) {
        super("Failed to save link with URL: " + linkDto.url());
    }
}
