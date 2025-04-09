package com.jve.DTO;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Integer id;
    private String username;
    private String nombre;
    private String apellidos;
    private String email;
    private String role;
    private String direccion;
    private String telefono;
    private String password;
} 