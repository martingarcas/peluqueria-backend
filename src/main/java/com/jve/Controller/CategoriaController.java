package com.jve.Controller;

import com.jve.DTO.CategoriaDTO;
import com.jve.Service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodas() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoriaService.obtenerTodas());
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoriaService.obtenerPorId(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping(consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(
            @Valid @RequestPart("categoria") CategoriaDTO categoriaDTO,
            @RequestPart(value = "productosNuevos", required = false) String productosNuevosJson,
            @RequestPart(value = "productosExistentesIds", required = false) String productosExistentesIdsJson,
            @RequestPart(value = "forzarMovimiento", required = false) String forzarMovimiento,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                categoriaService.crear(categoriaDTO, productosNuevosJson, productosExistentesIdsJson, 
                    Boolean.parseBoolean(forzarMovimiento), fotos)
            );
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestPart("categoria") CategoriaDTO categoriaDTO,
            @RequestPart(value = "productosNuevos", required = false) String productosNuevosJson,
            @RequestPart(value = "productosExistentesIds", required = false) String productosExistentesIdsJson,
            @RequestPart(value = "forzarMovimiento", required = false) String forzarMovimiento,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {
        try {
            categoriaDTO.setId(id);
            return ResponseEntity.ok(
                categoriaService.actualizar(id, categoriaDTO, productosNuevosJson, productosExistentesIdsJson, 
                    Boolean.parseBoolean(forzarMovimiento), fotos)
            );
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminar(
            @PathVariable Integer id,
            @RequestParam(required = false) Boolean eliminarProductos) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(categoriaService.eliminar(id, eliminarProductos));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 