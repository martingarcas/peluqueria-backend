package com.jve.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LoginRequestDTO(
    @NotNull(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "El email debe tener un formato válido")
    String email,

    @NotNull(message = "La contraseña es obligatoria")
    String password
) {
} 