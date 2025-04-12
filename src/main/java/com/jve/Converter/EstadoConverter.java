package com.jve.Converter;

import com.jve.DTO.EstadoDTO;
import com.jve.Entity.Estado;
import org.springframework.stereotype.Component;

@Component
public class EstadoConverter {

    public EstadoDTO toDTO(Estado estado) {
        if (estado == null) return null;
        
        return new EstadoDTO(
            estado.getId(),
            estado.getNombre(),
            estado.getTipoEstado()
        );
    }

    public Estado toEntity(EstadoDTO dto) {
        if (dto == null) return null;
        
        Estado estado = new Estado();
        if (dto.getId() != null) {
            estado.setId(dto.getId());
        }
        estado.setNombre(dto.getNombre());
        estado.setTipoEstado(dto.getTipoEstado());
        
        return estado;
    }
} 