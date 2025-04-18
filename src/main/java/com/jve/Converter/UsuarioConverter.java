package com.jve.Converter;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Entity.RolUsuario;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UsuarioConverter {

    private final ModelMapper modelMapper;

    public UsuarioConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

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
        return dto;
    }
} 