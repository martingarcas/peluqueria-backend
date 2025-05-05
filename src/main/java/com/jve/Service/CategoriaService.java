package com.jve.Service;

import com.jve.Converter.CategoriaConverter;
import com.jve.Converter.ProductoConverter;
import com.jve.DTO.CategoriaDTO;
import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Categoria;
import com.jve.Entity.Producto;
import com.jve.Repository.CategoriaRepository;
import com.jve.Repository.ProductoRepository;
import com.jve.Exception.ResourceNotFoundException;
import com.jve.Exception.ResourceAlreadyExistsException;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private static final String NOMBRE_PROTEGIDO = "Otros productos";

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaConverter converter;
    private final ProductoConverter productoConverter;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void initCategoriaGeneral() {
        if (!categoriaRepository.existsByNombre(NOMBRE_PROTEGIDO)) {
            Categoria categoriaGeneral = new Categoria();
            categoriaGeneral.setNombre(NOMBRE_PROTEGIDO);
            categoriaGeneral.setDescripcion("Categoría por defecto para productos no clasificados");
            categoriaRepository.save(categoriaGeneral);
        }
    }

    public Map<String, Object> obtenerTodas() {
        List<CategoriaDTO> categorias = categoriaRepository.findAll()
                .stream()
                .map(converter::toDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.CATEGORIAS_LISTADAS);
        response.put("categorias", categorias);
        return response;
    }

    public Map<String, Object> obtenerPorId(Integer id) {
        CategoriaDTO categoria = categoriaRepository.findById(id)
                .map(converter::toDTO)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Categoría encontrada con éxito");
        response.put("categoria", categoria);
        return response;
    }

    @Transactional
    public Map<String, Object> crear(CategoriaDTO categoriaDTO, String productosNuevosJson, 
            String productosExistentesIdsJson, Boolean forzarMovimiento, List<MultipartFile> fotos) {
        try {
            // Procesar productos nuevos si existen
            List<ProductoDTO> productosNuevos = new ArrayList<>();
            if (productosNuevosJson != null && !productosNuevosJson.isEmpty()) {
                productosNuevos = objectMapper.readValue(
                    productosNuevosJson, 
                    new TypeReference<List<ProductoDTO>>() {}
                );
                categoriaDTO.setProductosNuevos(productosNuevos);
            }

            // Procesar IDs de productos existentes si existen
            if (productosExistentesIdsJson != null && !productosExistentesIdsJson.isEmpty()) {
                List<Integer> productosExistentesIds = objectMapper.readValue(
                    productosExistentesIdsJson,
                    new TypeReference<List<Integer>>() {}
                );
                categoriaDTO.setProductosExistentesIds(productosExistentesIds);
            }

            // Establecer forzarMovimiento
            categoriaDTO.setForzarMovimiento(forzarMovimiento);

            // Llamar al método existente
            return crear(categoriaDTO, fotos);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar los datos de la categoría: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> crear(CategoriaDTO categoriaDTO, List<MultipartFile> fotos) {
        try {
            // Procesar las fotos primero si hay productos nuevos y fotos
            if (categoriaDTO.getProductosNuevos() != null && !categoriaDTO.getProductosNuevos().isEmpty() 
                && fotos != null && !fotos.isEmpty()) {
                
                // Crear directorio si no existe
                Path uploadPath = Paths.get("uploads/productos");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Procesar cada foto
                for (int i = 0; i < categoriaDTO.getProductosNuevos().size() && i < fotos.size(); i++) {
                    MultipartFile foto = fotos.get(i);
                    if (foto != null && !foto.isEmpty()) {
                        // Generar nombre único para el archivo
                        String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);

                        // Guardar el archivo
                        Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        // Establecer la URL de la foto en el DTO correspondiente
                        categoriaDTO.getProductosNuevos().get(i).setFoto("/uploads/productos/" + fileName);
                    }
                }
            }

            return crear(categoriaDTO);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar las fotos de los productos: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, CategoriaDTO categoriaDTO, String productosNuevosJson,
            String productosExistentesIdsJson, Boolean forzarMovimiento, List<MultipartFile> fotos) {
        try {
            // Procesar productos nuevos si existen
            List<ProductoDTO> productosNuevos = new ArrayList<>();
            if (productosNuevosJson != null && !productosNuevosJson.isEmpty()) {
                productosNuevos = objectMapper.readValue(
                    productosNuevosJson, 
                    new TypeReference<List<ProductoDTO>>() {}
                );
                // Asignar las fotos a los productos nuevos si hay fotos
                if (fotos != null && !fotos.isEmpty()) {
                    for (int i = 0; i < productosNuevos.size() && i < fotos.size(); i++) {
                        MultipartFile foto = fotos.get(i);
                        if (foto != null && !foto.isEmpty()) {
                            // Crear directorio si no existe
                            Path uploadPath = Paths.get("uploads/productos");
                            if (!Files.exists(uploadPath)) {
                                Files.createDirectories(uploadPath);
                            }

                            // Generar nombre único para el archivo
                            String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                            Path filePath = uploadPath.resolve(fileName);

                            // Guardar el archivo
                            Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                            // Establecer la URL de la foto en el DTO
                            productosNuevos.get(i).setFoto("/uploads/productos/" + fileName);
                        }
                    }
                }
                categoriaDTO.setProductosNuevos(productosNuevos);
            }

            // Procesar IDs de productos existentes si existen
            if (productosExistentesIdsJson != null && !productosExistentesIdsJson.isEmpty()) {
                List<Integer> productosExistentesIds = objectMapper.readValue(
                    productosExistentesIdsJson,
                    new TypeReference<List<Integer>>() {}
                );
                categoriaDTO.setProductosExistentesIds(productosExistentesIds);
            }

            // Establecer forzarMovimiento
            categoriaDTO.setForzarMovimiento(forzarMovimiento);

            // Llamar al método existente
            return actualizar(id, categoriaDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar los datos de la categoría: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> crear(CategoriaDTO categoriaDTO) {
        // Validar que no sea el nombre protegido
        if (categoriaDTO.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
            throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
        }

        // Validar que no exista el nombre de la categoría
        if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new RuntimeException(ValidationErrorMessages.CATEGORIA_NOMBRE_DUPLICADO);
        }

        // Validar que no existan productos con los mismos nombres
        if (categoriaDTO.getProductosNuevos() != null && !categoriaDTO.getProductosNuevos().isEmpty()) {
            List<String> productosExistentes = new ArrayList<>();
            for (ProductoDTO productoDTO : categoriaDTO.getProductosNuevos()) {
                if (productoRepository.existsByNombre(productoDTO.getNombre())) {
                    productosExistentes.add(productoDTO.getNombre());
                }
            }
            if (!productosExistentes.isEmpty()) {
                throw new RuntimeException(ValidationErrorMessages.PRODUCTO_YA_EXISTE + ": " + String.join(", ", productosExistentes));
            }
        }

        // Si hay productos existentes, validar que forzarMovimiento esté especificado
        if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
            if (categoriaDTO.getForzarMovimiento() == null) {
                throw new RuntimeException(ValidationErrorMessages.CATEGORIA_FORZAR_MOVIMIENTO);
            }
        }

        // Crear la categoría
        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setDescripcion(categoriaDTO.getDescripcion());
        Categoria categoriaFinal = categoriaRepository.save(categoria);

        Map<String, Object> response = new HashMap<>();
        StringBuilder mensaje = new StringBuilder("Categoría creada con éxito");

        // Crear y asignar productos nuevos si los hay
        if (categoriaDTO.getProductosNuevos() != null && !categoriaDTO.getProductosNuevos().isEmpty()) {
            for (ProductoDTO productoDTO : categoriaDTO.getProductosNuevos()) {
                System.out.println("Creando producto con foto: " + productoDTO.getFoto()); // Log para debug
                Producto producto = productoConverter.toEntity(productoDTO);
                producto.setCategoria(categoriaFinal);
                
                // Asegurarnos de que la foto se establezca
                if (productoDTO.getFoto() != null && !productoDTO.getFoto().isEmpty()) {
                    producto.setFoto(productoDTO.getFoto());
                    System.out.println("Foto establecida en entidad: " + producto.getFoto()); // Log para debug
                }
                
                productoRepository.save(producto);
            }
            mensaje.append(String.format(ValidationErrorMessages.CATEGORIA_PRODUCTOS_CREADOS, 
                categoriaDTO.getProductosNuevos().size()));
        }

        // Reasignar productos existentes si hay
        if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
            List<Producto> productosExistentes = productoRepository.findAllById(categoriaDTO.getProductosExistentesIds());
            
            // Verificar que todos los IDs existen
            if (productosExistentes.size() != categoriaDTO.getProductosExistentesIds().size()) {
                throw new RuntimeException(ValidationErrorMessages.CATEGORIA_PRODUCTOS_NO_EXISTEN);
            }

            // Separar productos con y sin categoría
            Map<String, String> productosConCategoria = new HashMap<>();
            List<Producto> productosSinCategoria = new ArrayList<>();

            for (Producto producto : productosExistentes) {
                if (producto.getCategoria() != null) {
                    productosConCategoria.put(producto.getNombre(), producto.getCategoria().getNombre());
                } else {
                    productosSinCategoria.add(producto);
                }
            }

            // Si hay productos con categoría y no se fuerza el movimiento
            if (!productosConCategoria.isEmpty() && !categoriaDTO.getForzarMovimiento()) {
                if (productosSinCategoria.isEmpty()) {
                    mensaje.append(". " + ValidationErrorMessages.CATEGORIA_NO_ASIGNADOS);
                } else {
                    mensaje.append(". " + ValidationErrorMessages.CATEGORIA_SOLO_SIN_CATEGORIA);
                }
                
                mensaje.append("ValidationErrorMessages.CATEGORIA_PRODUCTOS_MANTIENEN");
                productosConCategoria.forEach((producto, categoriaActual) -> 
                    mensaje.append(String.format("- %s (en '%s')", producto, categoriaActual))
                );
                
                // Si hay productos sin categoría, moverlos
                if (!productosSinCategoria.isEmpty()) {
                    productosSinCategoria.forEach(producto -> {
                        producto.setCategoria(categoriaFinal);
                        productoRepository.save(producto);
                    });
                    mensaje.append(ValidationErrorMessages.CATEGORIA_PRODUCTOS_ASIGNADOS);
                    productosSinCategoria.forEach(producto -> 
                        mensaje.append(String.format("- %s", producto.getNombre()))
                    );
                }
            } else {
                // Si se fuerza el movimiento o todos los productos están sin categoría, mover todos
                productosExistentes.forEach(producto -> {
                    String categoriaAnterior = producto.getCategoria() != null ? producto.getCategoria().getNombre() : "sin categoría";
                    producto.setCategoria(categoriaFinal);
                    productoRepository.save(producto);
                    mensaje.append(String.format(ValidationErrorMessages.CATEGORIA_PRODUCTO_MOVIDO, 
                        producto.getNombre(), categoriaAnterior));
                });
            }
        } else if (categoriaDTO.getProductosNuevos() == null || categoriaDTO.getProductosNuevos().isEmpty()) {
            mensaje.append(" " + ValidationErrorMessages.CATEGORIA_SIN_PRODUCTOS);
        }

        // Recargar la categoría para obtener todos los productos actualizados
        Categoria categoriaActualizada = categoriaRepository.findById(categoriaFinal.getId()).orElseThrow();
        response.put("categoria", converter.toDTO(categoriaActualizada));
        response.put("mensaje", mensaje.toString());
        return response;
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, CategoriaDTO categoriaDTO) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));

        // Para la categoría protegida, solo permitimos mover productos
        if (existente.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
            if (!existente.getNombre().equals(categoriaDTO.getNombre()) || 
                !existente.getDescripcion().equals(categoriaDTO.getDescripcion())) {
                throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
            }
        } else {
            // Si el nombre es diferente, validamos que no exista y que no sea el nombre protegido
            if (!existente.getNombre().equals(categoriaDTO.getNombre())) {
                if (categoriaDTO.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
                    throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
                }
                if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
                    throw new RuntimeException(ValidationErrorMessages.CATEGORIA_NOMBRE_DUPLICADO);
                }
            }
            
            existente.setNombre(categoriaDTO.getNombre());
            existente.setDescripcion(categoriaDTO.getDescripcion());
        }

        // Procesar productos existentes si los hay
        if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
            List<Producto> productosExistentes = productoRepository.findAllById(categoriaDTO.getProductosExistentesIds());
            
            // Verificar que todos los IDs existen
            if (productosExistentes.size() != categoriaDTO.getProductosExistentesIds().size()) {
                throw new RuntimeException(ValidationErrorMessages.CATEGORIA_PRODUCTOS_NO_EXISTEN);
            }

            // Separar productos con y sin categoría
            Map<String, String> productosConCategoria = new HashMap<>();
            List<Producto> productosSinCategoria = new ArrayList<>();

            for (Producto producto : productosExistentes) {
                if (producto.getCategoria() != null) {
                    productosConCategoria.put(producto.getNombre(), producto.getCategoria().getNombre());
                } else {
                    productosSinCategoria.add(producto);
                }
            }

            // Si hay productos con categoría y no se fuerza el movimiento
            if (!productosConCategoria.isEmpty() && (categoriaDTO.getForzarMovimiento() == null || !categoriaDTO.getForzarMovimiento())) {
                throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PRODUCTOS_CON_CATEGORIA, 
                    productosConCategoria.entrySet().stream()
                        .map(e -> e.getKey() + " (en " + e.getValue() + ")")
                        .collect(Collectors.joining(", "))));
            }

            // Mover todos los productos
            for (Producto producto : productosExistentes) {
                producto.setCategoria(existente);
                productoRepository.save(producto);
            }
        }
        
        Categoria updated = categoriaRepository.save(existente);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.CATEGORIA_ACTUALIZADA);
        response.put("categoria", converter.toDTO(updated));
        return response;
    }

    @Transactional
    public Map<String, Object> eliminar(Integer id, Boolean eliminarProductos) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));

        // Validar si es la categoría protegida
        if (categoria.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
            throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA_ELIMINAR, NOMBRE_PROTEGIDO));
        }

        // Verificar si tiene productos
        boolean tieneProductos = !categoria.getProductos().isEmpty();
        
        // Si tiene productos y no se especifica qué hacer con ellos
        if (tieneProductos && eliminarProductos == null) {
            throw new RuntimeException(ValidationErrorMessages.CATEGORIA_PRODUCTOS_ASOCIADOS);
        }

        Map<String, Object> response = new HashMap<>();

        // Si no tiene productos, ignorar el parámetro eliminarProductos
        if (!tieneProductos) {
            categoriaRepository.delete(categoria);
            response.put("mensaje", ResponseMessages.CATEGORIA_ELIMINADA);
            return response;
        }

        // Si tiene productos, proceder según lo especificado
        if (eliminarProductos) {
            categoriaRepository.delete(categoria);
            response.put("mensaje", ResponseMessages.CATEGORIA_ELIMINADA + " y sus productos");
            return response;
        } else {
            Categoria categoriaGeneral = categoriaRepository.findByNombreIgnoreCase(NOMBRE_PROTEGIDO)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CATEGORIA_PROTEGIDA_NO_EXISTE));
            
            List<Producto> productos = new ArrayList<>(categoria.getProductos());
            for (Producto producto : productos) {
                producto.setCategoria(categoriaGeneral);
                productoRepository.save(producto);
            }
            
            categoria.getProductos().clear();
            categoriaRepository.delete(categoria);
            response.put("mensaje", ResponseMessages.CATEGORIA_ELIMINADA + ". Los productos se movieron a 'Otros productos'");
            return response;
        }
    }
} 