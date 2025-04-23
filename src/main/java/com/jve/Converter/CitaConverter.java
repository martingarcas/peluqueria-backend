package com.jve.Converter;

import com.jve.DTO.CitaDTO;
import com.jve.Entity.Cita;
import com.jve.Entity.Estado;
import com.jve.Entity.Servicio;
import com.jve.Entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CitaConverter {
    
    public CitaDTO.CitaRequest toDto(Cita cita) {
        if (cita == null) return null;
        
        CitaDTO.CitaRequest dto = new CitaDTO.CitaRequest();
        dto.setId(cita.getId());
        dto.setServicioId(cita.getServicio().getId());
        dto.setServicioNombre(cita.getServicio().getNombre());
        dto.setTrabajadorId(cita.getTrabajador().getId());
        dto.setTrabajadorNombre(cita.getTrabajador().getNombre() + " " + cita.getTrabajador().getApellidos());
        dto.setUsuarioId(cita.getUsuario().getId());
        dto.setUsuarioNombre(cita.getUsuario().getNombre() + " " + cita.getUsuario().getApellidos());
        dto.setFecha(cita.getFecha());
        dto.setHoraInicio(cita.getHoraInicio());
        dto.setHoraFin(cita.getHoraFin());
        dto.setEstado(cita.getEstado().getNombre());
        
        return dto;
    }
    
    public List<CitaDTO.CitaRequest> toDtoList(List<Cita> citas) {
        if (citas == null) return new ArrayList<>();
        return citas.stream().map(this::toDto).toList();
    }
    
    public Cita toEntity(CitaDTO.CitaRequest dto, Usuario usuario, Usuario trabajador, 
                        Servicio servicio, Estado estado) {
        if (dto == null) return null;
        
        Cita cita = new Cita();
        cita.setId(dto.getId());
        cita.setUsuario(usuario);
        cita.setTrabajador(trabajador);
        cita.setServicio(servicio);
        cita.setFecha(dto.getFecha());
        cita.setHoraInicio(dto.getHoraInicio());
        cita.setHoraFin(dto.getHoraFin());
        cita.setEstado(estado);
        
        return cita;
    }
} 