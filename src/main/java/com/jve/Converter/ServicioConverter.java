package com.jve.Converter;

import com.jve.DTO.ServicioDTO;
import com.jve.Entity.Servicio;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServicioConverter {

    public ServicioDTO toDTO(Servicio servicio) {
        if (servicio == null) return null;
        
        ServicioDTO dto = new ServicioDTO();
        dto.setId(servicio.getId());
        dto.setNombre(servicio.getNombre());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setDuracion(servicio.getDuracion());
        dto.setPrecio(servicio.getPrecio());
        
        if (servicio.getUsuarios() != null) {
            dto.setTrabajadoresIds(servicio.getUsuarios().stream()
                    .map(usuario -> usuario.getId())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    public Servicio toEntity(ServicioDTO dto) {
        if (dto == null) return null;
        
        Servicio servicio = new Servicio();
        if (dto.getId() != null) {
            servicio.setId(dto.getId());
        }
        servicio.setNombre(dto.getNombre());
        servicio.setDescripcion(dto.getDescripcion());
        servicio.setDuracion(dto.getDuracion());
        servicio.setPrecio(dto.getPrecio());
        
        return servicio;
    }
} 