package com.jve.DTO;

import com.jve.Entity.DiaSemana;
import com.jve.Exception.ValidationErrorMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Time;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioDTO {
    private Integer id;

    @NotBlank(message = ValidationErrorMessages.HORARIO_NOMBRE_REQUERIDO)
    private String nombre;

    @NotNull(message = ValidationErrorMessages.HORARIO_DIA_REQUERIDO)
    private DiaSemana diaSemana;

    @NotNull(message = ValidationErrorMessages.HORARIO_HORA_INICIO_REQUERIDA)
    private Time horaInicio;

    @NotNull(message = ValidationErrorMessages.HORARIO_HORA_FIN_REQUERIDA)
    private Time horaFin;

    private Set<Integer> trabajadorIds;
    
    private Set<String> nombresTrabajadores;
} 