package com.jve.DTO;

import com.jve.Entity.TipoEstado;
import com.jve.Exception.ValidationErrorMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoDTO {
    
    private Integer id;

    @NotBlank(message = ValidationErrorMessages.ESTADO_NOMBRE_REQUERIDO)
    private String nombre;

    @NotNull(message = ValidationErrorMessages.ESTADO_TIPO_REQUERIDO)
    private TipoEstado tipoEstado;
} 