package com.linkvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record LinkDto(
    Long id,

    @URL(message = "URL must be valid")
    @NotBlank(message = "URL is required")
    @Size(max = 255, message = "URL must be at most 255 characters")
    String url,

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be at most 100 characters")
    String title,

    @Size(max = 255, message = "Description must be at most 255 characters")
    String description,

    @NotNull(message = "User ID is required")
    Long userId
) {}
