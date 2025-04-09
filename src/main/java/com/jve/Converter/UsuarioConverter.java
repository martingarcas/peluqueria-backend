package com.jve.Converter;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
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
        return modelMapper.map(usuarioDTO, Usuario.class);
    }

    public RegistroResponseDTO toResponseDTO(Usuario usuario) {
        return modelMapper.map(usuario, RegistroResponseDTO.class);
    }
} 