package com.jve.DTO;

import com.jve.Entity.DiaSemana;
import lombok.Data;
import java.sql.Time;
import java.util.Set;

@Data
public class HorarioDTO {
    private Integer id;
    private String nombre;
    private DiaSemana diaSemana;
    private Time horaInicio;
    private Time horaFin;
    private Set<Integer> trabajadorIds;
    private Set<String> nombresTrabajadores;
} 