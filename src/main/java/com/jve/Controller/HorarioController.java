package com.jve.Controller;

import com.jve.DTO.HorarioDTO;
import com.jve.Service.HorarioService;
import com.jve.Exception.ValidationErrorMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listarTodos() {
        try {
            Map<String, Object> response = horarioService.listarTodos();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        try {
            Map<String, Object> response = horarioService.obtenerPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(
            @Valid @RequestBody HorarioDTO horarioDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage()
                ));
            response.put("mensaje", "Error de validación");
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> response = horarioService.crear(horarioDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody HorarioDTO horarioDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage()
                ));
            response.put("mensaje", "Error de validación");
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> response = horarioService.actualizar(id, horarioDTO);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ex.getReason());
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        try {
            Map<String, Object> response = horarioService.eliminar(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, String> response = new HashMap<>();
        if (ex.getMessage().contains("DiaSemana")) {
            response.put("error", ValidationErrorMessages.DIA_SEMANA_INVALIDO);
        } else {
            response.put("error", ValidationErrorMessages.ERROR_FORMATO_JSON);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
} 