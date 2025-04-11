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
import java.util.HashMap;

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
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(categoriaService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No se encontró la categoría con id: " + id));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        try {
            Map<String, Object> response = categoriaService.crear(categoriaDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
    public ResponseEntity<?> eliminar(
            @PathVariable Integer id,
            @RequestParam(required = false) Boolean eliminarProductos) {
        try {
            String mensaje = categoriaService.eliminar(id, eliminarProductos);
            return ResponseEntity.ok().body(Map.of("mensaje", mensaje));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 