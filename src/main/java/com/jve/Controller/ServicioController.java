package com.jve.Controller;

import com.jve.DTO.ServicioDTO;
import com.jve.Service.ServicioService;
import com.jve.Exception.ResponseMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(@Valid @RequestBody ServicioDTO servicioDTO, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> 
                errores.put(error.getField(), error.getDefaultMessage())
            );
            response.put("error", "Error de validación");
            response.put("detalles", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            return new ResponseEntity<>(servicioService.crear(servicioDTO), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(servicioService.obtenerPorId(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTodos() {
        try {
            return ResponseEntity.ok(servicioService.listarTodos());
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/trabajador/{trabajadorId}")
    public ResponseEntity<Map<String, Object>> listarPorTrabajador(@PathVariable Integer trabajadorId) {
        try {
            return ResponseEntity.ok(servicioService.listarPorTrabajador(trabajadorId));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ServicioDTO servicioDTO,
            BindingResult result) {
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = new HashMap<>();
            result.getFieldErrors().forEach(error -> 
                errores.put(error.getField(), error.getDefaultMessage())
            );
            response.put("error", "Error de validación");
            response.put("detalles", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> response = servicioService.actualizar(id, servicioDTO);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            throw ex;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(servicioService.eliminar(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/trabajador/{trabajadorId}/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> asignarServiciosATrabajador(
            @PathVariable Integer trabajadorId,
            @RequestBody List<Integer> serviciosIds) {
        try {
            Map<String, Object> response = servicioService.asignarServiciosATrabajador(trabajadorId, serviciosIds);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 