package com.jve.Converter;

import com.jve.DTO.CategoriaDTO;
import com.jve.Entity.Categoria;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoriaConverter {

    private final ModelMapper modelMapper;

    public CategoriaConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CategoriaDTO toDTO(Categoria categoria) {
        return modelMapper.map(categoria, CategoriaDTO.class);
    }

    public Categoria toEntity(CategoriaDTO categoriaDTO) {
        return modelMapper.map(categoriaDTO, Categoria.class);
    }
} 