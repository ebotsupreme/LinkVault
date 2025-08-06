package com.linkvault.dto;

public record LinkResponse(
    Long id,
    String url,
    String title,
    String description,
    Long userId
) {}
