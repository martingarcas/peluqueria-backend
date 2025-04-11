package com.jve.Service;

import com.jve.Converter.ProductoConverter;
import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Producto;
import com.jve.Repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductoService {
    
    private final ProductoRepository productoRepository;
    private final ProductoConverter converter;

    private void validarDatosProducto(String nombre, String descripcion, BigDecimal precio, Integer stock) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new RuntimeException("La descripción es obligatoria");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio debe ser positivo");
        }
        if (stock == null || stock <= 0) {
            throw new RuntimeException("El stock debe ser positivo");
        }
    }

    @Transactional
    public ProductoDTO crear(ProductoDTO productoDTO) {
        // Validar datos
        validarDatosProducto(
            productoDTO.getNombre(),
            productoDTO.getDescripcion(),
            productoDTO.getPrecio(),
            productoDTO.getStock()
        );

        // Validar nombre único
        if (productoRepository.existsByNombre(productoDTO.getNombre())) {
            throw new RuntimeException("Ya existe un producto con este nombre");
        }

        // Convertir y guardar
        Producto producto = converter.toEntity(productoDTO);
        Producto guardado = productoRepository.save(producto);
        return converter.toDTO(guardado);
    }
} 