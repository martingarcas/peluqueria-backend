package com.jve.Controller;

import com.jve.DTO.CategoriaDTO;
import com.jve.Service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final ObjectMapper objectMapper;

    public CategoriaController(CategoriaService categoriaService, ObjectMapper objectMapper) {
        this.categoriaService = categoriaService;
        this.objectMapper = objectMapper;
    }

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
    public ResponseEntity<Map<String, Object>> create(
            @RequestPart("categoria") String categoriaJson,
            @RequestPart(value = "productosNuevos", required = false) String productosNuevosJson,
            @RequestPart(value = "productosExistentesIds", required = false) String productosExistentesIdsJson,
            @RequestPart(value = "forzarMovimiento", required = false) String forzarMovimiento,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {
        try {
            Map<String, Object> response = categoriaService.crearCategoria(
                categoriaJson, 
                productosNuevosJson, 
                productosExistentesIdsJson, 
                forzarMovimiento != null ? Boolean.parseBoolean(forzarMovimiento) : null, 
                fotos
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response  = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @RequestPart("categoria") String categoriaJson,
            @RequestPart(value = "productosNuevos", required = false) String productosNuevosJson,
            @RequestPart(value = "productosExistentesIds", required = false) String productosExistentesIdsJson,
            @RequestPart(value = "forzarMovimiento", required = false) String forzarMovimiento,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {
        try {
            return ResponseEntity.ok(
                categoriaService.actualizar(id, categoriaJson, productosNuevosJson, productosExistentesIdsJson, 
                    forzarMovimiento != null ? Boolean.parseBoolean(forzarMovimiento) : null, fotos)
            );
        } catch (Exception e) {
            Map<String, Object> response  = new HashMap<>();
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