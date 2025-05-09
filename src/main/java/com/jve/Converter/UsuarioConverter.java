package com.jve.Converter;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.Servicio;
import com.jve.Entity.Horario;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsuarioConverter {

    private final ModelMapper modelMapper;

    public UsuarioDTO toDTO(Usuario usuario) {
        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    public Usuario toEntity(UsuarioDTO usuarioDTO) {
        Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
        if (usuarioDTO.getRole() != null) {
            usuario.setRol(RolUsuario.valueOf(usuarioDTO.getRole().toLowerCase()));
        }
        return usuario;
    }

    public RegistroResponseDTO toResponseDTO(Usuario usuario) {
        RegistroResponseDTO dto = modelMapper.map(usuario, RegistroResponseDTO.class);
        dto.setRole(usuario.getRol().name());
        dto.setFechaRegistro(usuario.getFechaRegistro() != null ? 
            usuario.getFechaRegistro().toString() : null);
        
        // Convertir servicios y horarios si el usuario es un trabajador
        if (usuario.getRol() == RolUsuario.trabajador) {
            if (usuario.getServicios() != null) {
                dto.setServicios(usuario.getServicios().stream()
                    .map(servicio -> new RegistroResponseDTO.ServicioSimpleDTO(
                        servicio.getId(),
                        servicio.getNombre()
                    ))
                    .collect(Collectors.toList()));
            }
            
            if (usuario.getHorarios() != null) {
                dto.setHorarios(usuario.getHorarios().stream()
                    .map(horario -> new RegistroResponseDTO.HorarioSimpleDTO(
                        horario.getId(),
                        horario.getDiaSemana().toString(),
                        horario.getHoraInicio().toString(),
                        horario.getHoraFin().toString()
                    ))
                    .collect(Collectors.toList()));
            }
        }
        
        return dto;
    }
} 