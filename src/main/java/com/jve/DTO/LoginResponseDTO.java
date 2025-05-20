package com.jve.DTO;

import java.util.List;

public record LoginResponseDTO(
    Integer id,
    String username,
    List<String> authorities,
    String token,
    String nombre,
    String apellidos,
    String email,
    String role,
    String direccion,
    String telefono,
    String foto
) {
} 