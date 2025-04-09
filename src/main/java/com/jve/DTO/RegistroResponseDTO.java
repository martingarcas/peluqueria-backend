package com.jve.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResponseDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String role;
    private String direccion;
    private String telefono;
} 