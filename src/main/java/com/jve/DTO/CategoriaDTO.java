package com.jve.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDTO {
    private Integer id;
    private String nombre;
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