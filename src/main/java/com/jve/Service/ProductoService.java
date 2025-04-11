package com.jve.Service;

import com.jve.Converter.ProductoConverter;
import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Producto;
import com.jve.Repository.ProductoRepository;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {
    
    private final ProductoRepository productoRepository;
    private final ProductoConverter converter;

    private void validarDatosProducto(String nombre, String descripcion, BigDecimal precio, Integer stock) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.PRODUCTO_NOMBRE_REQUERIDO);
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.PRODUCTO_DESCRIPCION_REQUERIDA);
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(ValidationErrorMessages.PRODUCTO_PRECIO_POSITIVO);
        }
        if (stock == null || stock < 0) {
            throw new RuntimeException(ValidationErrorMessages.PRODUCTO_STOCK_MINIMO);
        }
    }

    public Map<String, Object> obtenerTodos() {
        List<ProductoDTO> productos = productoRepository.findAll()
            .stream()
            .map(converter::toDTO)
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.PRODUCTOS_LISTADOS);
        response.put("productos", productos);
        return response;
    }

    @Transactional
    public Map<String, Object> crear(ProductoDTO productoDTO) {
        // Validar datos
        validarDatosProducto(
            productoDTO.getNombre(),
            productoDTO.getDescripcion(),
            productoDTO.getPrecio(),
            productoDTO.getStock()
        );

        // Validar nombre único
        if (productoRepository.existsByNombre(productoDTO.getNombre())) {
            throw new RuntimeException(ValidationErrorMessages.PRODUCTO_YA_EXISTE);
        }

        // Convertir y guardar
        Producto producto = converter.toEntity(productoDTO);
        Producto guardado = productoRepository.save(producto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.PRODUCTO_CREADO);
        response.put("producto", converter.toDTO(guardado));
        return response;
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, ProductoDTO productoDTO) {
        Producto existente = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se encontró el producto con id: " + id));

        // Si todos los datos son idénticos, no hacemos update
        if (existente.getNombre().equals(productoDTO.getNombre()) &&
            existente.getDescripcion().equals(productoDTO.getDescripcion()) &&
            existente.getPrecio().equals(productoDTO.getPrecio()) &&
            existente.getStock().equals(productoDTO.getStock())) {
            throw new RuntimeException(ResponseMessages.PRODUCTO_NO_CAMBIOS);
        }

        // Validar datos
        validarDatosProducto(
            productoDTO.getNombre(),
            productoDTO.getDescripcion(),
            productoDTO.getPrecio(),
            productoDTO.getStock()
        );

        // Si el nombre es diferente, validar que no exista
        if (!existente.getNombre().equals(productoDTO.getNombre()) &&
            productoRepository.existsByNombre(productoDTO.getNombre())) {
            throw new RuntimeException(ValidationErrorMessages.PRODUCTO_YA_EXISTE);
        }

        existente.setNombre(productoDTO.getNombre());
        existente.setDescripcion(productoDTO.getDescripcion());
        existente.setPrecio(productoDTO.getPrecio());
        existente.setStock(productoDTO.getStock());

        Producto actualizado = productoRepository.save(existente);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.PRODUCTO_ACTUALIZADO);
        response.put("producto", converter.toDTO(actualizado));
        return response;
    }

    @Transactional
    public Map<String, Object> eliminar(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("No se encontró el producto con id: " + id);
        }
        
        productoRepository.deleteById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.PRODUCTO_ELIMINADO);
        return response;
    }
} 