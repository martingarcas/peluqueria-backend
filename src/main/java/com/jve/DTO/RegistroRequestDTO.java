package com.jve.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
DTO con la información necesaria para registrar un nuevo usuario en base de datos
{
    "password": "contraseña123",
    "email": "usuario1@example.com",
    "nombre": "Juan",
    "apellidos": "Pérez",
    "direccion": "Calle Principal 123",
    "telefono": "123456789"
}
 */
public record RegistroRequestDTO(
    @NotNull(message = "La contraseña es obligatoria") 
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    String password,
    
    @NotNull(message = "El email es obligatorio") 
    @Email(message = "El email debe tener un formato válido")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "El email debe tener un formato válido")
    String email,
    
    @NotNull(message = "El nombre es obligatorio") 
    String nombre,
    
    @NotNull(message = "Los apellidos son obligatorios") 
    String apellidos,
    
    @NotNull(message = "La dirección es obligatoria") 
    String direccion,
    
    @NotNull(message = "El teléfono es obligatorio") 
    String telefono
) {} 