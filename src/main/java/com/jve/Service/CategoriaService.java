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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private static final String NOMBRE_PROTEGIDO = "Otros productos";

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaConverter converter;
    private final ProductoConverter productoConverter;

    @PostConstruct
    public void initCategoriaGeneral() {
        if (!categoriaRepository.existsByNombre(NOMBRE_PROTEGIDO)) {
            Categoria categoriaGeneral = new Categoria();
            categoriaGeneral.setNombre(NOMBRE_PROTEGIDO);
            categoriaGeneral.setDescripcion("Categoría por defecto para productos no clasificados");
            categoriaRepository.save(categoriaGeneral);
        }
    }

    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(converter::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO obtenerPorId(Integer id) {
        return categoriaRepository.findById(id)
                .map(converter::toDTO)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));
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
                Producto producto = productoConverter.toEntity(productoDTO);
                producto.setCategoria(categoriaFinal);
                productoRepository.save(producto);
            }
            mensaje.append(String.format("\\nSe crearon %d productos nuevos en la categoría", 
                categoriaDTO.getProductosNuevos().size()));
        }

        // Reasignar productos existentes si hay
        if (categoriaDTO.getProductosExistentesIds() != null && !categoriaDTO.getProductosExistentesIds().isEmpty()) {
            List<Producto> productosExistentes = productoRepository.findAllById(categoriaDTO.getProductosExistentesIds());
            
            // Verificar que todos los IDs existen
            if (productosExistentes.size() != categoriaDTO.getProductosExistentesIds().size()) {
                throw new RuntimeException("Algunos IDs de productos no existen");
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
                    mensaje.append(". No se asignaron productos existentes ya que todos pertenecen a otras categorías");
                } else {
                    mensaje.append(". Solo se asignaron los productos existentes que no tenían categoría");
                }
                
                mensaje.append("\\nProductos que mantienen su categoría actual:\\n");
                productosConCategoria.forEach((producto, categoriaActual) -> 
                    mensaje.append(String.format("- %s (en '%s')\\n", producto, categoriaActual))
                );
                
                // Si hay productos sin categoría, moverlos
                if (!productosSinCategoria.isEmpty()) {
                    productosSinCategoria.forEach(producto -> {
                        producto.setCategoria(categoriaFinal);
                        productoRepository.save(producto);
                    });
                    mensaje.append("\\nProductos existentes asignados a la nueva categoría:\\n");
                    productosSinCategoria.forEach(producto -> 
                        mensaje.append(String.format("- %s\\n", producto.getNombre()))
                    );
                }
            } else {
                // Si se fuerza el movimiento o todos los productos están sin categoría, mover todos
                productosExistentes.forEach(producto -> {
                    String categoriaAnterior = producto.getCategoria() != null ? producto.getCategoria().getNombre() : "sin categoría";
                    producto.setCategoria(categoriaFinal);
                    productoRepository.save(producto);
                    mensaje.append(String.format("\\n- Producto existente '%s' movido desde '%s'", producto.getNombre(), categoriaAnterior));
                });
            }
        } else if (categoriaDTO.getProductosNuevos() == null || categoriaDTO.getProductosNuevos().isEmpty()) {
            mensaje.append(" sin productos asociados");
        }

        // Recargar la categoría para obtener todos los productos actualizados
        Categoria categoriaActualizada = categoriaRepository.findById(categoriaFinal.getId()).orElseThrow();
        response.put("categoria", converter.toDTO(categoriaActualizada));
        response.put("mensaje", mensaje.toString());
        return response;
    }

    @Transactional
    public CategoriaDTO actualizar(Integer id, CategoriaDTO categoriaDTO) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));

        // Validar si es la categoría protegida
        if (existente.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
            throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
        }

        // Si tanto nombre como descripción son idénticos, no hacemos update
        if (existente.getNombre().equals(categoriaDTO.getNombre()) && 
            existente.getDescripcion().equals(categoriaDTO.getDescripcion())) {
            throw new RuntimeException("La categoría ya existe con el mismo nombre '" + categoriaDTO.getNombre() + 
                "' y la misma descripción. No se requieren cambios.");
        }

        // Si el nombre es diferente, validamos que no exista
        if (!existente.getNombre().equals(categoriaDTO.getNombre()) && 
            categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new RuntimeException(ValidationErrorMessages.CATEGORIA_NOMBRE_DUPLICADO);
        }

        existente.setNombre(categoriaDTO.getNombre());
        existente.setDescripcion(categoriaDTO.getDescripcion());
        
        Categoria updated = categoriaRepository.save(existente);
        return converter.toDTO(updated);
    }

    @Transactional
    public String eliminar(Integer id, Boolean eliminarProductos) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_NO_ENCONTRADA, id)));

        // Validar si es la categoría protegida
        if (categoria.getNombre().equalsIgnoreCase(NOMBRE_PROTEGIDO)) {
            throw new RuntimeException(String.format(ValidationErrorMessages.CATEGORIA_PROTEGIDA, NOMBRE_PROTEGIDO));
        }

        // Verificar si tiene productos
        boolean tieneProductos = !categoria.getProductos().isEmpty();
        
        // Si tiene productos y no se especifica qué hacer con ellos
        if (tieneProductos && eliminarProductos == null) {
            throw new RuntimeException("La categoría tiene productos asociados. Debes especificar 'eliminarProductos=true' para eliminar los productos junto con la categoría, o 'eliminarProductos=false' para moverlos a la categoría 'Otros productos'");
        }

        // Si no tiene productos, ignorar el parámetro eliminarProductos
        if (!tieneProductos) {
            categoriaRepository.delete(categoria);
            return "Categoría eliminada con éxito";
        }

        // Si tiene productos, proceder según lo especificado
        if (eliminarProductos) {
            categoriaRepository.delete(categoria);
            return "Categoría eliminada con éxito y sus productos";
        } else {
            Categoria categoriaGeneral = categoriaRepository.findByNombreIgnoreCase(NOMBRE_PROTEGIDO)
                .orElseThrow(() -> new RuntimeException("La categoría protegida no existe"));
            
            List<Producto> productos = new ArrayList<>(categoria.getProductos());
            for (Producto producto : productos) {
                producto.setCategoria(categoriaGeneral);
                productoRepository.save(producto);
            }
            
            categoria.getProductos().clear();
            categoriaRepository.delete(categoria);
            return "Categoría eliminada con éxito. Los productos se movieron a 'Otros productos'";
        }
    }
} 