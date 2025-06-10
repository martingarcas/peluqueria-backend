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
import com.jve.Entity.Categoria;
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
            // 1. Manejar la foto
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String urlFoto = "/uploads/productos/" + fileName;

            // 2. Validar datos
            validarDatosProducto(
                productoDTO.getNombre(),
                productoDTO.getDescripcion(),
                productoDTO.getPrecio(),
                productoDTO.getStock()
            );

            // 3. Validar nombre único
            if (productoRepository.existsByNombre(productoDTO.getNombre())) {
                throw new RuntimeException(ValidationErrorMessages.PRODUCTO_YA_EXISTE);
            }

            // 4. Convertir a entidad
            Producto producto = converter.toEntity(productoDTO);
            producto.setFoto(urlFoto);
            
            // 5. Asignar categoría
            if (productoDTO.getCategoriaId() != null) {
                Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA));
                producto.setCategoria(categoria);
            } else {
                Categoria categoriaDefault = categoriaRepository.findByNombreIgnoreCase("Otros productos")
                    .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CATEGORIA_PROTEGIDA_NO_EXISTE));
                producto.setCategoria(categoriaDefault);
            }

            // 6. Guardar y devolver
            Producto guardado = productoRepository.save(producto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ResponseMessages.PRODUCTO_CREADO);
            response.put("producto", converter.toDTO(guardado));
            return response;

        } catch (IOException e) {
            throw new RuntimeException(ValidationErrorMessages.ERROR_VALIDACION + ": " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, ProductoDTO productoDTO, MultipartFile foto) {
        try {
            // 1. Obtener producto existente
            Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.PRODUCTO_NO_ENCONTRADO));

            // 2. Manejar la foto si existe
            if (foto != null && !foto.isEmpty()) {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                String urlFoto = "/uploads/productos/" + fileName;
                existente.setFoto(urlFoto);
            }

            // 3. Validar datos
            validarDatosProducto(
                productoDTO.getNombre(),
                productoDTO.getDescripcion(),
                productoDTO.getPrecio(),
                productoDTO.getStock()
            );

            // 4. Validar nombre único si cambió
            if (!existente.getNombre().equals(productoDTO.getNombre()) &&
                productoRepository.existsByNombre(productoDTO.getNombre())) {
                throw new RuntimeException(ValidationErrorMessages.PRODUCTO_YA_EXISTE);
            }

            // 5. Actualizar datos básicos
            existente.setNombre(productoDTO.getNombre());
            existente.setDescripcion(productoDTO.getDescripcion());
            existente.setPrecio(productoDTO.getPrecio());
            existente.setStock(productoDTO.getStock());

            // 6. Actualizar categoría si se proporciona
            if (productoDTO.getCategoriaId() != null) {
                Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA));
                existente.setCategoria(categoria);
            }

            // 7. Guardar y devolver
            Producto actualizado = productoRepository.save(existente);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ResponseMessages.PRODUCTO_ACTUALIZADO);
            response.put("producto", converter.toDTO(actualizado));
            return response;

        } catch (IOException e) {
            throw new RuntimeException(ValidationErrorMessages.ERROR_VALIDACION + ": " + e.getMessage());
        }
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