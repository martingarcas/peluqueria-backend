package com.jve.DTO;

import com.jve.Exception.ValidationErrorMessages;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class UsuarioDTO {
    private Integer id;
    
    private String username;

    @NotBlank(message = ValidationErrorMessages.AUTH_NOMBRE_REQUERIDO)
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;

    @NotBlank(message = ValidationErrorMessages.AUTH_APELLIDOS_REQUERIDOS)
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;

    @NotBlank(message = ValidationErrorMessages.AUTH_EMAIL_REQUERIDO)
    @Email(message = ValidationErrorMessages.AUTH_EMAIL_FORMATO)
    private String email;

    private String role;

    @NotBlank(message = ValidationErrorMessages.AUTH_DIRECCION_REQUERIDA)
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    private String direccion;

    @NotBlank(message = ValidationErrorMessages.AUTH_TELEFONO_REQUERIDO)
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @NotBlank(message = ValidationErrorMessages.AUTH_PASSWORD_REQUERIDO)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = ValidationErrorMessages.AUTH_PASSWORD_FORMATO
    )
    private String password;
} 