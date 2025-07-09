package com.linkvault.exception;

import com.linkvault.model.Link;

public class LinkSaveException extends RuntimeException {
    public LinkSaveException(Link link) {
        super("Failed to save link with URL: " + link.getUrl());
    }
}
