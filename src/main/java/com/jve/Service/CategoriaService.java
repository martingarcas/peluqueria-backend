package com.jve.Service;

import com.jve.Converter.CategoriaConverter;
import com.jve.DTO.CategoriaDTO;
import com.jve.Entity.Categoria;
import com.jve.Repository.CategoriaRepository;
import com.jve.Exception.ResourceNotFoundException;
import com.jve.Exception.ResourceAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaConverter converter;

    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(converter::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO obtenerPorId(Integer id) {
        return categoriaRepository.findById(id)
                .map(converter::toDTO)
                .orElseThrow(() -> new RuntimeException("No se encontró la categoría con id: " + id));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaDTO categoriaDTO) {
        if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con este nombre");
        }
        Categoria categoria = converter.toEntity(categoriaDTO);
        Categoria savedCategoria = categoriaRepository.save(categoria);
        return converter.toDTO(savedCategoria);
    }

    @Transactional
    public CategoriaDTO actualizar(Integer id, CategoriaDTO categoriaDTO) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la categoría con id: " + id));

        if (existente.getNombre().equals(categoriaDTO.getNombre()) && 
            existente.getDescripcion().equals(categoriaDTO.getDescripcion())) {
            throw new RuntimeException("La categoría ya existe con el mismo nombre '" + categoriaDTO.getNombre() + 
                "' y la misma descripción. No se requieren cambios.");
        }

        if (!existente.getNombre().equals(categoriaDTO.getNombre()) && 
            categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con este nombre");
        }

        existente.setNombre(categoriaDTO.getNombre());
        existente.setDescripcion(categoriaDTO.getDescripcion());
        
        Categoria updated = categoriaRepository.save(existente);
        return converter.toDTO(updated);
    }

    @Transactional
    public void eliminar(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("No se encontró la categoría con id: " + id);
        }
        categoriaRepository.deleteById(id);
    }
} 