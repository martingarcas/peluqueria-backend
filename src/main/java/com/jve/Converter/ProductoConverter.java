package com.jve.Converter;

import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoConverter {

    public ProductoDTO toDTO(Producto producto) {
        if (producto == null) return null;
        
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setFoto(producto.getFoto());
        dto.setFechaCreacion(producto.getFechaCreacion());
        
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }
        
        return dto;
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
        producto.setFoto(dto.getFoto());
        
        return producto;
    }
}