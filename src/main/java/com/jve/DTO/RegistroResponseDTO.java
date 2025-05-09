package com.jve.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroResponseDTO {
    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String role;
    private String direccion;
    private String telefono;
    private String foto;
    private List<ServicioSimpleDTO> servicios;
    private List<HorarioSimpleDTO> horarios;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicioSimpleDTO {
        private Integer id;
        private String nombre;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HorarioSimpleDTO {
        private Integer id;
        private String dia;
        private String horaInicio;
        private String horaFin;
    }
} 