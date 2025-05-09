package com.jve.Converter;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.DTO.TrabajadorResponseDTO;
import com.jve.DTO.ServicioSimpleDTO;
import com.jve.DTO.HorarioSimpleDTO;
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
        RegistroResponseDTO dto;
        
        if (usuario.getRol() == RolUsuario.trabajador) {
            TrabajadorResponseDTO trabajadorDTO = modelMapper.map(usuario, TrabajadorResponseDTO.class);
            if (usuario.getServicios() != null) {
                trabajadorDTO.setServicios(usuario.getServicios().stream()
                    .map(servicio -> new ServicioSimpleDTO(
                        servicio.getId(),
                        servicio.getNombre()
                    ))
                    .collect(Collectors.toList()));
            }
            
            if (usuario.getHorarios() != null) {
                trabajadorDTO.setHorarios(usuario.getHorarios().stream()
                    .map(horario -> new HorarioSimpleDTO(
                        horario.getId(),
                        horario.getDiaSemana().toString(),
                        horario.getHoraInicio().toString(),
                        horario.getHoraFin().toString()
                    ))
                    .collect(Collectors.toList()));
            }
            dto = trabajadorDTO;
        } else {
            dto = modelMapper.map(usuario, RegistroResponseDTO.class);
        }
        
        dto.setRole(usuario.getRol().name());
        dto.setFechaRegistro(usuario.getFechaRegistro() != null ? 
            usuario.getFechaRegistro().toString() : null);
            
        return dto;
    }
} 