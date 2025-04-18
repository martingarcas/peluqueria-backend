package com.jve.DTO;

import com.jve.Entity.TipoContrato;
import com.jve.Exception.ValidationErrorMessages;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;
import java.util.Date;
import java.math.BigDecimal;

@Data
public class ContratoDTO {
    private Integer id;
    
    @NotNull(message = ValidationErrorMessages.USUARIO_NO_ENCONTRADO)
    private Integer usuarioId;
    
    private String nombreUsuario;

    @NotNull(message = ValidationErrorMessages.CONTRATO_FECHA_INICIO_REQUERIDA)
    @FutureOrPresent(message = ValidationErrorMessages.CONTRATO_FECHA_INICIO_PASADA)
    private Date fechaInicioContrato;

    private Date fechaFinContrato;

    @NotNull(message = ValidationErrorMessages.CONTRATO_TIPO_REQUERIDO)
    private TipoContrato tipoContrato;

    private Integer estadoId;
    
    private String estadoNombre;

    private String urlContrato;

    @NotNull(message = ValidationErrorMessages.CONTRATO_SALARIO_REQUERIDO)
    @Positive(message = ValidationErrorMessages.CONTRATO_SALARIO_NEGATIVO)
    private BigDecimal salario;
} 