package com.jve.Converter;

import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Producto;
import com.jve.Repository.CategoriaRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductoConverter {

    private final CategoriaRepository categoriaRepository;

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
        
        // Manejo de categoría
        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId())
                .ifPresentOrElse(
                    producto::setCategoria,
                    () -> {
                        throw new RuntimeException(
                            String.format("La categoría con ID %d no existe", dto.getCategoriaId())
                        );
                    }
                );
        } else {
            // Si no se especifica categoría, buscar la categoría "Otros productos"
            categoriaRepository.findByNombreIgnoreCase("Otros productos")
                .ifPresent(producto::setCategoria);
        }
        
        return producto;
    }
} 