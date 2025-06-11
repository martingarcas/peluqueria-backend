package com.jve.Service;

import com.jve.Converter.CategoriaConverter;
import com.jve.Converter.ProductoConverter;
import com.jve.DTO.CategoriaDTO;
import com.jve.DTO.ProductoDTO;
import com.jve.Entity.Categoria;
import com.jve.Entity.Producto;
import com.jve.Repository.CategoriaRepository;
import com.jve.Repository.ProductoRepository;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private static final String NOMBRE_PROTEGIDO = "Otros productos";

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaConverter converter;
    private final ProductoConverter productoConverter;
    private final ObjectMapper objectMapper;
    private final ProductoService productoService;

    @PostConstruct
    public void init() {
        try {
            if (!categoriaRepository.existsByNombre(NOMBRE_PROTEGIDO)) {
                Categoria categoria = new Categoria();
                categoria.setNombre(NOMBRE_PROTEGIDO);
                categoria.setDescripcion("Categoría para productos sin clasificar");
                categoriaRepository.save(categoria);
            }
        } catch (Exception e) {
            throw new RuntimeException(ValidationErrorMessages.CATEGORIA_PROTEGIDA_NO_EXISTE);
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
    public Map<String, Object> crearCategoria(String categoriaJson, String productosNuevosJson, 
            String productosExistentesIdsJson, Boolean forzarMovimiento, List<MultipartFile> fotos) {
        try {
            // 1. Convertir JSON a DTO
            CategoriaDTO categoriaDTO = objectMapper.readValue(categoriaJson, CategoriaDTO.class);
            
            // 2. Procesar JSONs
            procesarDatosFormData(categoriaDTO, productosNuevosJson, productosExistentesIdsJson, forzarMovimiento);
            
            // 3. Validar que no sea el nombre protegido
            if (categoriaDTO.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
                throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
            }

            // 4. Validar que no exista el nombre de la categoría
            if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
                throw new RuntimeException(ValidationErrorMessages.CATEGORIA_NOMBRE_DUPLICADO);
            }

            // 5. Si hay productos existentes, validar que forzarMovimiento esté especificado
            if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
                if (categoriaDTO.getForzarMovimiento() == null) {
                    throw new RuntimeException(ValidationErrorMessages.CATEGORIA_FORZAR_MOVIMIENTO);
                }
            }

            // 6. Crear la categoría
            Categoria categoria = new Categoria();
            categoria.setNombre(categoriaDTO.getNombre());
            categoria.setDescripcion(categoriaDTO.getDescripcion());
            Categoria categoriaFinal = categoriaRepository.save(categoria);

            Map<String, Object> response = new HashMap<>();
            StringBuilder mensaje = new StringBuilder(ResponseMessages.CATEGORIA_CREADA);

            // 7. Crear y asignar productos nuevos si los hay
            if (categoriaDTO.getProductosNuevos() != null && !categoriaDTO.getProductosNuevos().isEmpty()) {
                for (int i = 0; i < categoriaDTO.getProductosNuevos().size(); i++) {
                    ProductoDTO productoDTO = categoriaDTO.getProductosNuevos().get(i);
                    productoDTO.setCategoriaId(categoriaFinal.getId());
                    MultipartFile foto = fotos != null && i < fotos.size() ? fotos.get(i) : null;
                    productoService.crear(productoDTO, foto);
                }
                mensaje.append(String.format(ValidationErrorMessages.CATEGORIA_PRODUCTOS_CREADOS, 
                    categoriaDTO.getProductosNuevos().size()));
            }

            // 8. Reasignar productos existentes si hay
            if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
                // 1. Obtener todos los productos existentes por sus IDs
                List<Producto> productosExistentes = productoRepository.findAllById(categoriaDTO.getProductosExistentesIds());
                 // 2. Verificar que todos los IDs existen
                if (productosExistentes.size() != categoriaDTO.getProductosExistentesIds().size()) {
                    throw new RuntimeException(ValidationErrorMessages.CATEGORIA_PRODUCTOS_NO_EXISTEN);
                }

                // 3. Separar productos con y sin categoría
                Map<String, String> productosConCategoria = new HashMap<>();
                List<Producto> productosSinCategoria = new ArrayList<>();

                for (Producto producto : productosExistentes) {
                    if (producto.getCategoria() != null) {
                        // Productos que ya tienen categoría
                        productosConCategoria.put(producto.getNombre(), producto.getCategoria().getNombre());
                    } else {
                        // Productos sin categoría
                        productosSinCategoria.add(producto);
                    }
                }

                // 4. Lógica de reasignación
                if (!productosConCategoria.isEmpty()) {  // Si hay productos con categoría
                    if (categoriaDTO.getForzarMovimiento()) {
                        // CASO 1: Forzar movimiento - Mover todos los productos
                        productosExistentes.forEach(producto -> {
                            String categoriaAnterior = producto.getCategoria() != null ? 
                                producto.getCategoria().getNombre() : "sin categoría";
                            ProductoDTO productoDTO = productoConverter.toDTO(producto);
                            productoDTO.setCategoriaId(categoriaFinal.getId());
                            productoService.actualizar(producto.getId(), productoDTO, null);
                            mensaje.append(String.format(ValidationErrorMessages.CATEGORIA_PRODUCTO_MOVIDO, 
                                producto.getNombre(), categoriaAnterior));
                        });
                    } else {
                        // CASO 2: No forzar movimiento - Mover solo los sin categoría
                        if (productosSinCategoria.isEmpty()) {
                            mensaje.append(". " + ValidationErrorMessages.CATEGORIA_NO_ASIGNADOS);
                        } else {
                            mensaje.append(". " + ValidationErrorMessages.CATEGORIA_SOLO_SIN_CATEGORIA);
                            
                            // Mover los productos sin categoría
                            productosSinCategoria.forEach(producto -> {
                                ProductoDTO productoDTO = productoConverter.toDTO(producto);
                                productoDTO.setCategoriaId(categoriaFinal.getId());
                                productoService.actualizar(producto.getId(), productoDTO, null);
                            });
                            
                            mensaje.append(ValidationErrorMessages.CATEGORIA_PRODUCTOS_ASIGNADOS);
                            productosSinCategoria.forEach(producto -> 
                                mensaje.append(String.format("- %s", producto.getNombre()))
                            );
                        }
                    }
                } else {
                    // CASO 3: Todos los productos están sin categoría - Mover todos directamente
                    productosExistentes.forEach(producto -> {
                        ProductoDTO productoDTO = productoConverter.toDTO(producto);
                        productoDTO.setCategoriaId(categoriaFinal.getId());
                        productoService.actualizar(producto.getId(), productoDTO, null);
                    });
                    mensaje.append(ValidationErrorMessages.CATEGORIA_PRODUCTOS_ASIGNADOS);
                    productosExistentes.forEach(producto -> 
                        mensaje.append(String.format("- %s", producto.getNombre()))
                    );
                }

                // CASO 4: No hay productos nuevos ni existentes
            } else if (categoriaDTO.getProductosNuevos() == null || categoriaDTO.getProductosNuevos().isEmpty()) {
                mensaje.append(" " + ValidationErrorMessages.CATEGORIA_SIN_PRODUCTOS);
            }

            // 9. Recargar la categoría para obtener todos los productos actualizados
            Categoria categoriaActualizada = categoriaRepository.findById(categoriaFinal.getId()).orElseThrow();
            response.put("categoria", converter.toDTO(categoriaActualizada));
            response.put("mensaje", mensaje.toString());
            return response;

        } catch (JsonProcessingException e) {
            throw new RuntimeException(ValidationErrorMessages.ERROR_FORMATO_JSON);
        } catch (IOException e) {
            throw new RuntimeException(ValidationErrorMessages.ERROR_VALIDACION);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void procesarDatosFormData(CategoriaDTO categoriaDTO, String productosNuevosJson, 
            String productosExistentesIdsJson, Boolean forzarMovimiento) throws JsonMappingException, JsonProcessingException {
        // 1. Procesar productos nuevos
        if (productosNuevosJson != null && !productosNuevosJson.isEmpty()) {
            List<ProductoDTO> productosNuevos = objectMapper.readValue(
                productosNuevosJson, 
                new TypeReference<List<ProductoDTO>>() {}
            );
            categoriaDTO.setProductosNuevos(productosNuevos);
        }

        // 2. Procesar IDs de productos existentes
        if (productosExistentesIdsJson != null && !productosExistentesIdsJson.isEmpty()) {
            List<Integer> productosExistentesIds = objectMapper.readValue(
                productosExistentesIdsJson,
                new TypeReference<List<Integer>>() {}
            );
            categoriaDTO.setProductosExistentesIds(productosExistentesIds);
        }

        // 3. Establecer forzarMovimiento
        categoriaDTO.setForzarMovimiento(forzarMovimiento);
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, String categoriaJson, String productosNuevosJson, 
            String productosExistentesIdsJson, Boolean forzarMovimiento, List<MultipartFile> fotos) {
        try {
            // 1. Convertir JSON a DTO
            CategoriaDTO categoriaDTO = objectMapper.readValue(categoriaJson, CategoriaDTO.class);
            categoriaDTO.setId(id);
            
            // 2. Procesar productos nuevos si existen
            if (productosNuevosJson != null && !productosNuevosJson.isEmpty()) {
                List<ProductoDTO> productosNuevos = objectMapper.readValue(
                    productosNuevosJson, 
                    new TypeReference<List<ProductoDTO>>() {}
                );
                
                // Procesar fotos para productos nuevos
                if (fotos != null && !fotos.isEmpty()) {
                    Path uploadPath = Paths.get("uploads/productos");
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    for (int i = 0; i < productosNuevos.size() && i < fotos.size(); i++) {
                        MultipartFile foto = fotos.get(i);
                        if (foto != null && !foto.isEmpty()) {
                            String fileName = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                            Path filePath = uploadPath.resolve(fileName);
                            Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                            productosNuevos.get(i).setFoto("/uploads/productos/" + fileName);
                        }
                    }
                }
                categoriaDTO.setProductosNuevos(productosNuevos);
            }

            // 3. Procesar IDs de productos existentes
            if (productosExistentesIdsJson != null && !productosExistentesIdsJson.isEmpty()) {
                List<Integer> productosExistentesIds = objectMapper.readValue(
                    productosExistentesIdsJson,
                    new TypeReference<List<Integer>>() {}
                );
                categoriaDTO.setProductosExistentesIds(productosExistentesIds);
            }

            // 4. Establecer forzarMovimiento
            categoriaDTO.setForzarMovimiento(forzarMovimiento);

            // 5. Obtener categoría existente
            Categoria existente = categoriaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));

            // 6. Validaciones de categoría protegida
            if (existente.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
                if (!existente.getNombre().equals(categoriaDTO.getNombre()) || 
                    !existente.getDescripcion().equals(categoriaDTO.getDescripcion())) {
                    throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
                }
            } else {
                // Validar nombre duplicado
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

            // 7. Procesar productos existentes
            if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
                List<Producto> productosExistentes = productoRepository.findAllById(categoriaDTO.getProductosExistentesIds());
                
                if (productosExistentes.size() != categoriaDTO.getProductosExistentesIds().size()) {
                    throw new RuntimeException(ValidationErrorMessages.CATEGORIA_PRODUCTOS_NO_EXISTEN);
                }

                // Validar productos con categoría
                Map<String, String> productosConCategoria = new HashMap<>();
                for (Producto producto : productosExistentes) {
                    if (producto.getCategoria() != null) {
                        productosConCategoria.put(producto.getNombre(), producto.getCategoria().getNombre());
                    }
                }

                if (!productosConCategoria.isEmpty() && (categoriaDTO.getForzarMovimiento() == null || !categoriaDTO.getForzarMovimiento())) {
                    throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PRODUCTOS_CON_CATEGORIA, 
                        productosConCategoria.entrySet().stream()
                            .map(e -> e.getKey() + " (en " + e.getValue() + ")")
                            .collect(Collectors.joining(", "))));
                }

                // Mover productos
                for (Producto producto : productosExistentes) {
                    producto.setCategoria(existente);
                    productoRepository.save(producto);
                }
            }
            
            // 8. Guardar cambios
            Categoria updated = categoriaRepository.save(existente);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ResponseMessages.CATEGORIA_ACTUALIZADA);
            response.put("categoria", converter.toDTO(updated));
            return response;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al procesar los datos JSON: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar las fotos: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la actualización de la categoría: " + e.getMessage());
        }
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