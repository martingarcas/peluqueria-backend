package com.jve.DTO;

import com.jve.Entity.TipoContrato;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;

@Data
public class ContratoDTO {
    private Integer id;
    
    private Integer usuarioId;
    
    private String nombreUsuario;

    private Date fechaInicioContrato;

    private Date fechaFinContrato;

    private TipoContrato tipoContrato;

    private Integer estadoId;
    
    private String estadoNombre;

    private String urlContrato;
} 