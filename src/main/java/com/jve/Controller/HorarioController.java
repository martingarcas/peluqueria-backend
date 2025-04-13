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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listarTodos() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(horarioService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@Valid @RequestBody HorarioDTO horarioDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error de validación");
            response.put("detalles", bindingResult.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            return ResponseEntity.ok(horarioService.crear(horarioDTO));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody HorarioDTO horarioDTO,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error de validación");
            response.put("detalles", bindingResult.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            return ResponseEntity.ok(horarioService.actualizar(id, horarioDTO));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(horarioService.eliminar(id));
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