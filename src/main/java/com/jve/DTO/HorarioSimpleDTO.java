package com.jve.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioSimpleDTO {
    private Integer id;
    private String dia;
    private String horaInicio;
    private String horaFin;
} 