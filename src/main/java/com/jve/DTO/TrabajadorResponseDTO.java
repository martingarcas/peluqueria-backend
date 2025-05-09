package com.jve.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorResponseDTO extends RegistroResponseDTO {
    private List<ServicioSimpleDTO> servicios;
    private List<HorarioSimpleDTO> horarios;
} 