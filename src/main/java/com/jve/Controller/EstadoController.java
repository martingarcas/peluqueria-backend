package com.jve.Controller;

import com.jve.DTO.EstadoDTO;
import com.jve.Entity.TipoEstado;
import com.jve.Service.EstadoService;
import com.jve.Exception.ResponseMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EstadoController {

    private final EstadoService estadoService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerEstados(
            @RequestParam(required = false) String tipo) {
        if (tipo != null) {
            try {
                TipoEstado.valueOf(tipo);
                return ResponseEntity.ok(estadoService.obtenerPorTipo(tipo));
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "El tipo de estado debe ser uno de: CITA, CONTRATO, PEDIDO");
                return ResponseEntity.badRequest().body(response);
            }
        }
        return ResponseEntity.ok(estadoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(estadoService.obtenerPorId(id));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Map<String, Object> response = new HashMap<>();
        if (e.getMessage().contains("TipoEstado")) {
            response.put("error", "El tipo de estado debe ser uno de: CITA, CONTRATO, PEDIDO");
        } else {
            response.put("error", "Error en el formato de la petición");
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crear(
            @Valid @RequestBody EstadoDTO estadoDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errores.put(error.getField(), error.getDefaultMessage())
            );
            response.put("error", "Error de validación");
            response.put("detalles", errores);
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(estadoService.crear(estadoDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EstadoDTO estadoDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errores.put(error.getField(), error.getDefaultMessage())
            );
            response.put("error", "Error de validación");
            response.put("detalles", errores);
            return ResponseEntity.badRequest().body(response);
        }
        try {
            Map<String, Object> response = estadoService.actualizar(id, estadoDTO);
            if (response.get("mensaje").equals(ResponseMessages.NO_CAMBIOS_NECESARIOS)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(estadoService.eliminar(id));
    }
} 