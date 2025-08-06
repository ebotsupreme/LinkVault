package com.linkvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LinkRequest(
    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    @Size(max = 255, message = "URL must be at most 255 characters")
    String url,

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    String title,

    @Size(max = 255, message = "Description must be at most 255 characters")
    String description
) {}
