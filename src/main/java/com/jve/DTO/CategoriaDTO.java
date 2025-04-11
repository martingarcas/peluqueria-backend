package com.jve.DTO;

import com.jve.Exception.ValidationErrorMessages;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {
    private Integer id;

    @NotBlank(message = ValidationErrorMessages.CATEGORIA_NOMBRE_REQUERIDO)
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @NotBlank(message = ValidationErrorMessages.CATEGORIA_DESCRIPCION_REQUERIDA)
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    private String descripcion;
    
    // Campos solo para entrada (no aparecerán en la respuesta)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Integer> productosExistentesIds;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean forzarMovimiento;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<ProductoDTO> productosNuevos;
    
    // Campo solo para respuestas
    private List<ProductoDTO> productos;

    // Constructor para respuestas
    public CategoriaDTO(Integer id, String nombre, String descripcion, List<ProductoDTO> productos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.productos = productos;
    }
} 