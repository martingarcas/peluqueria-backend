package com.jve.Controller;

import com.jve.DTO.CategoriaDTO;
import com.jve.Service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> obtenerTodas() {
        List<CategoriaDTO> categorias = categoriaService.obtenerTodas();
        return categorias.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(categoriaService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        try {
            CategoriaDTO nuevaCategoria = categoriaService.crear(categoriaDTO);
            return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody CategoriaDTO categoriaDTO) {
        try {
            return ResponseEntity.ok(categoriaService.actualizar(id, categoriaDTO));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("La categoría ya existe con el mismo nombre")) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            categoriaService.eliminar(id);
            return ResponseEntity.ok().body(Map.of("mensaje", "Categoría eliminada con éxito"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", "No se encontró la categoría con ID: " + id));
        }
    }
} 