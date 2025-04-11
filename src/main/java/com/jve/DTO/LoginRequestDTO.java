package com.jve.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.jve.Exception.ValidationErrorMessages;

public record LoginRequestDTO(
    @NotBlank(message = ValidationErrorMessages.AUTH_EMAIL_REQUERIDO)
    @Email(message = ValidationErrorMessages.AUTH_EMAIL_FORMATO)
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = ValidationErrorMessages.AUTH_EMAIL_FORMATO)
    String email,

    @NotBlank(message = ValidationErrorMessages.AUTH_PASSWORD_REQUERIDO)
    String password
) {
} 