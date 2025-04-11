package com.jve.Converter;

import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoConverter {

    public ProductoDTO toDTO(Producto producto) {
        if (producto == null) return null;
        
        return new ProductoDTO(
            producto.getId(),
            producto.getNombre(),
            producto.getDescripcion(),
            producto.getPrecio(),
            producto.getStock(),
            producto.getCategoria() != null ? producto.getCategoria().getId() : null,
            producto.getCategoria() != null ? producto.getCategoria().getNombre() : null
        );
    }

    public Producto toEntity(ProductoDTO dto) {
        if (dto == null) return null;
        
        Producto producto = new Producto();
        if (dto.getId() != null) {
            producto.setId(dto.getId());
        }
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        
        return producto;
    }
} 