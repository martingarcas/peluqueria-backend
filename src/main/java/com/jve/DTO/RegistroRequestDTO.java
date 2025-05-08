package com.jve.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.jve.Entity.RolUsuario;
import com.jve.Exception.ValidationErrorMessages;

/*
DTO con la información necesaria para registrar un nuevo usuario en base de datos
{
    "password": "contraseña123",
    "email": "usuario1@example.com",
    "nombre": "Juan",
    "apellidos": "Pérez",
    "direccion": "Calle Principal 123",
    "telefono": "123456789",
    "rol": "admin"
}
 */
public record RegistroRequestDTO(
    @NotBlank(message = ValidationErrorMessages.AUTH_PASSWORD_REQUERIDO) 
    @Size(min = 8, message = ValidationErrorMessages.AUTH_PASSWORD_LONGITUD)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = ValidationErrorMessages.AUTH_PASSWORD_FORMATO
    )
    String password,
    
    @NotBlank(message = ValidationErrorMessages.AUTH_EMAIL_REQUERIDO) 
    @Email(message = ValidationErrorMessages.AUTH_EMAIL_FORMATO)
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = ValidationErrorMessages.AUTH_EMAIL_FORMATO)
    String email,
    
    @NotBlank(message = ValidationErrorMessages.AUTH_NOMBRE_REQUERIDO) 
    String nombre,
    
    @NotBlank(message = ValidationErrorMessages.AUTH_APELLIDOS_REQUERIDOS) 
    String apellidos,
    
    String direccion,
    
    @NotBlank(message = ValidationErrorMessages.AUTH_TELEFONO_REQUERIDO) 
    String telefono,

    RolUsuario rol
) {} 