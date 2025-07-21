package com.linkvault.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "User name is required")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
