package com.jve.DTO;

import com.jve.Exception.ValidationErrorMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    
    private Integer id;

    @NotBlank(message = ValidationErrorMessages.SERVICIO_NOMBRE_REQUERIDO)
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = ValidationErrorMessages.SERVICIO_DESCRIPCION_REQUERIDA)
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    private String descripcion;

    @NotNull(message = ValidationErrorMessages.SERVICIO_DURACION_REQUERIDA)
    @Positive(message = ValidationErrorMessages.SERVICIO_DURACION_POSITIVA)
    private Integer duracion;

    @NotNull(message = ValidationErrorMessages.SERVICIO_PRECIO_REQUERIDO)
    @Positive(message = ValidationErrorMessages.SERVICIO_PRECIO_POSITIVO)
    private BigDecimal precio;

    private List<Integer> trabajadoresIds;
} 