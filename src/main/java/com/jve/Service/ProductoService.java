package com.jve.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jve.Converter.ProductoConverter;
import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Producto;
import com.jve.Exception.ResponseMessages;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Repository.CategoriaRepository;
import com.jve.Repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {
    
    private final ProductoRepository productoRepository;
    private final ProductoConverter converter;
    private final CategoriaRepository categoriaRepository;
    private final String UPLOAD_DIR = "uploads/productos/";

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
    public Map<String, Object> crear(ProductoDTO productoDTO, MultipartFile foto) {
        try {
            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para el archivo
            String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Guardar el archivo
            Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Establecer la URL de la foto
            productoDTO.setFoto("/uploads/productos/" + fileName);

            Map<String, Object> response = crear(productoDTO);
            response.put("mensaje", ResponseMessages.PRODUCTO_CREADO);
            return response;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto del producto: " + e.getMessage());
        }
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
    public Map<String, Object> actualizar(Integer id, ProductoDTO productoDTO, MultipartFile foto) {
        try {
            // Si hay foto nueva, procesarla primero
            if (foto != null && !foto.isEmpty()) {
                // Crear directorio si no existe
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generar nombre único para el archivo
                String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Guardar el archivo
                Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Establecer la URL de la foto
                productoDTO.setFoto("/uploads/productos/" + fileName);
            }

            // Continuar con la actualización normal
            return actualizar(id, productoDTO);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la foto del producto: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, ProductoDTO productoDTO) {
        Producto existente = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se encontró el producto con id: " + id));

        // Si todos los datos son idénticos, incluyendo la categoría, no hacemos update
        boolean mismaCategoria = (existente.getCategoria() == null && productoDTO.getCategoriaId() == null) ||
                               (existente.getCategoria() != null && 
                                productoDTO.getCategoriaId() != null && 
                                existente.getCategoria().getId().equals(productoDTO.getCategoriaId()));

        boolean mismaFoto = (existente.getFoto() == null && (productoDTO.getFoto() == null || productoDTO.getFoto().isEmpty())) ||
                          (existente.getFoto() != null && existente.getFoto().equals(productoDTO.getFoto()));

        if (existente.getNombre().equals(productoDTO.getNombre()) &&
            existente.getDescripcion().equals(productoDTO.getDescripcion()) &&
            existente.getPrecio().equals(productoDTO.getPrecio()) &&
            existente.getStock().equals(productoDTO.getStock()) &&
            mismaCategoria &&
            mismaFoto) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ValidationErrorMessages.PRODUCTO_NO_CAMBIOS);
            response.put("producto", converter.toDTO(existente));
            return response;
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

        // Actualizar datos básicos
        existente.setNombre(productoDTO.getNombre());
        existente.setDescripcion(productoDTO.getDescripcion());
        existente.setPrecio(productoDTO.getPrecio());
        existente.setStock(productoDTO.getStock());

        // Actualizar foto si se proporciona una nueva
        if (productoDTO.getFoto() != null && !productoDTO.getFoto().isEmpty()) {
            existente.setFoto(productoDTO.getFoto());
        }

        // Actualizar categoría solo si se envía una nueva
        if (productoDTO.getCategoriaId() != null) {
            categoriaRepository.findById(productoDTO.getCategoriaId())
                .ifPresentOrElse(
                    categoria -> existente.setCategoria(categoria),
                    () -> {
                        throw new RuntimeException("No se encontró la categoría con id: " + productoDTO.getCategoriaId());
                    }
                );
        }
        // Si no se envía categoriaId, mantener la categoría existente

        Producto actualizado = productoRepository.save(existente);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.PRODUCTO_ACTUALIZADO);
        response.put("producto", converter.toDTO(actualizado));
        return response;
    }

    @Transactional
    public Map<String, Object> eliminar(Integer id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("No se encontró el producto con id: " + id));

        // Eliminar la foto si existe
        if (producto.getFoto() != null) {
            try {
                Path fotoPath = Paths.get(producto.getFoto().replace("/uploads/", "uploads/"));
                Files.deleteIfExists(fotoPath);
            } catch (IOException e) {
                // Log error pero continuar con la eliminación
                System.err.println("Error eliminando foto: " + e.getMessage());
            }
        }

        productoRepository.delete(producto);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.PRODUCTO_ELIMINADO);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Producto recuperado exitosamente");
        response.put("producto", converter.toDTO(producto));
        return response;
    }
} 