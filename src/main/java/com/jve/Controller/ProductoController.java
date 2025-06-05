package com.jve.Controller;

import com.jve.DTO.ProductoDTO;
import com.jve.Service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos() {
        try {
            Map<String, Object> response = productoService.obtenerTodos();
            if (response.get("productos") == null) {
                response.put("mensaje", "No hay productos disponibles");
            } else {
                response.put("mensaje", "Productos recuperados con éxito");
            }
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        try {
            Map<String, Object> response = productoService.obtenerPorId(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(
            @Valid @RequestPart("producto") ProductoDTO productoDTO,
            @RequestPart("foto") MultipartFile foto) {
        try {
            Map<String, Object> response = productoService.crear(productoDTO, foto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestPart("producto") ProductoDTO productoDTO,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        try {
            productoDTO.setId(id);
            Map<String, Object> response = productoService.actualizar(id, productoDTO, foto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        try {
            Map<String, Object> response = productoService.eliminar(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}