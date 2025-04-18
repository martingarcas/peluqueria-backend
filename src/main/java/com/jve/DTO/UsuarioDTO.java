package com.jve.DTO;

import com.jve.Exception.ValidationErrorMessages;
import com.jve.Entity.RolUsuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @NotBlank(message = ValidationErrorMessages.AUTH_DIRECCION_REQUERIDA)
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    private String direccion;

    @NotBlank(message = ValidationErrorMessages.AUTH_TELEFONO_REQUERIDO)
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = ValidationErrorMessages.AUTH_PASSWORD_REQUERIDO)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = ValidationErrorMessages.AUTH_PASSWORD_FORMATO
    )
    private String password;

    private String role;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnore(value = false)
    private String foto;
    
    private String carrito;

    // Campos específicos para trabajadores
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ContratoDTO contrato;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private MultipartFile documentoContrato;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 1, message = "Debe asignar al menos un servicio al trabajador")
    private List<Integer> serviciosIds;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 1, message = "Debe asignar al menos un horario al trabajador")
    private List<Integer> horariosIds;

    // Constructor para respuestas
    public UsuarioDTO(Integer id, String nombre, String apellidos, String email, 
                     String direccion, String telefono, String role, String foto) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.direccion = direccion;
        this.telefono = telefono;
        this.role = role;
        this.foto = foto;
    }

    // Método para validar si hay modificaciones
    public boolean tieneModificaciones(UsuarioDTO otro) {
        if (otro == null) return true;
        return !Objects.equals(this.nombre, otro.nombre) ||
               !Objects.equals(this.apellidos, otro.apellidos) ||
               !Objects.equals(this.email, otro.email) ||
               !Objects.equals(this.direccion, otro.direccion) ||
               !Objects.equals(this.telefono, otro.telefono) ||
               !Objects.equals(this.role, otro.role);
    }
} 