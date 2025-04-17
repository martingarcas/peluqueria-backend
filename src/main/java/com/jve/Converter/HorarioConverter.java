package com.jve.Converter;

import com.jve.DTO.HorarioDTO;
import com.jve.Entity.Horario;
import com.jve.Entity.Usuario;
import com.jve.Repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HorarioConverter {
    
    private final UsuarioRepository usuarioRepository;

    public HorarioDTO toDTO(Horario horario) {
        HorarioDTO dto = new HorarioDTO();
        dto.setId(horario.getId());
        dto.setNombre(horario.getNombre());
        dto.setDiaSemana(horario.getDiaSemana());
        dto.setHoraInicio(horario.getHoraInicio());
        dto.setHoraFin(horario.getHoraFin());
        
        if (horario.getTrabajadores() != null) {
            dto.setTrabajadorIds(horario.getTrabajadores().stream()
                .map(Usuario::getId)
                .collect(Collectors.toSet()));
            dto.setNombresTrabajadores(horario.getTrabajadores().stream()
                .map(Usuario::getNombre)
                .collect(Collectors.toSet()));
        }
        
        return dto;
    }

    public Horario toEntity(HorarioDTO dto) {
        Horario horario = new Horario();
        horario.setId(dto.getId());
        horario.setNombre(dto.getNombre());
        horario.setDiaSemana(dto.getDiaSemana());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFin(dto.getHoraFin());
        
        if (dto.getTrabajadorIds() != null) {
            horario.setTrabajadores(dto.getTrabajadorIds().stream()
                .map(id -> usuarioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Trabajador no encontrado")))
                .collect(Collectors.toList()));
        }
        
        return horario;
    }
} 