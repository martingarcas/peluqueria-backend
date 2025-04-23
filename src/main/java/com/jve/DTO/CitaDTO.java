package com.jve.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jve.Exception.ValidationErrorMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {
    @NotEmpty(message = ValidationErrorMessages.CITA_LISTA_VACIA)
    @Valid
    private List<CitaRequest> citas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitaRequest {
        private Integer id;

        @NotNull(message = ValidationErrorMessages.CITA_SERVICIO_REQUERIDO)
        private Integer servicioId;
        
        private String servicioNombre;

        @NotNull(message = ValidationErrorMessages.CITA_TRABAJADOR_REQUERIDO)
        private Integer trabajadorId;
        
        private String trabajadorNombre;

        @NotNull(message = ValidationErrorMessages.CITA_FECHA_REQUERIDA)
        @FutureOrPresent(message = ValidationErrorMessages.CITA_FECHA_PASADA)
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Madrid")
        private Date fecha;

        @NotNull(message = ValidationErrorMessages.CITA_HORA_REQUERIDA)
        @JsonFormat(pattern = "HH:mm:ss", timezone = "Europe/Madrid")
        private Time horaInicio;
        
        @JsonFormat(pattern = "HH:mm:ss", timezone = "Europe/Madrid")
        private Time horaFin;
        
        private String estado;
        
        private Integer usuarioId;
        
        private String usuarioNombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReasignacionRequest {
        @NotNull(message = ValidationErrorMessages.CITA_TRABAJADOR_REQUERIDO)
        private Integer trabajadorId;

        @NotNull(message = ValidationErrorMessages.CITA_FECHA_REQUERIDA)
        @FutureOrPresent(message = ValidationErrorMessages.CITA_FECHA_PASADA)
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Madrid")
        private Date fecha;

        @NotNull(message = ValidationErrorMessages.CITA_HORA_REQUERIDA)
        @JsonFormat(pattern = "HH:mm:ss", timezone = "Europe/Madrid")
        private Time horaInicio;
    }
} 