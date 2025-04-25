package com.jve.Converter;

import com.jve.DTO.CategoriaDTO;
import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Categoria;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.List;

@Component
public class CategoriaConverter {

    private ProductoConverter productoConverter;

    public CategoriaConverter(ProductoConverter productoConverter) {
        this.productoConverter = productoConverter;
    }

    public CategoriaDTO toDTO(Categoria categoria) {
        if (categoria == null) return null;
        
        List<ProductoDTO> productosDTO = categoria.getProductos() != null ? 
            categoria.getProductos().stream()
                .map(productoConverter::toDTO)
                .collect(Collectors.toList()) : 
            null;

        return new CategoriaDTO(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            productosDTO
        );
    }

    public Categoria toEntity(CategoriaDTO dto) {
        if (dto == null) return null;
        
        Categoria categoria = new Categoria();
        if (dto.getId() != null) {
            categoria.setId(dto.getId());
        }
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        
        return categoria;
    }
} 