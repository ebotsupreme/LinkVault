package com.linkvault.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException (Long linkId) {
        super("Link with ID " + linkId + " not found.");
    }

    public LinkNotFoundException (Long linkId, Throwable cause) {
        super("Link with ID " + linkId + " not found.", cause);
    }
}
