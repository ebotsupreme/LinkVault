package com.linkvault.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException (Long linkId) {
        super("Link with ID " + linkId + " not found.");
    }
}
