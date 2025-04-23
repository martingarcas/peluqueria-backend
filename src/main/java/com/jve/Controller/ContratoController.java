package com.jve.Controller;

import com.jve.DTO.ContratoDTO;
import com.jve.Entity.TipoContrato;
import com.jve.Service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerTodos() {
        return ResponseEntity.ok(contratoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(contratoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPorUsuarioId(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(contratoService.obtenerPorUsuarioId(usuarioId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(
            @RequestParam("usuarioId") Integer usuarioId,
            @RequestParam("fechaInicioContrato") String fechaInicioContrato,
            @RequestParam(value = "fechaFinContrato", required = false) String fechaFinContrato,
            @RequestParam("tipoContrato") TipoContrato tipoContrato,
            @RequestPart("documento") MultipartFile documento) {
        
        try {
            ContratoDTO contratoDTO = new ContratoDTO();
            contratoDTO.setUsuarioId(usuarioId);
            contratoDTO.setFechaInicioContrato(java.sql.Date.valueOf(fechaInicioContrato));
            if (fechaFinContrato != null && !fechaFinContrato.isEmpty()) {
                contratoDTO.setFechaFinContrato(java.sql.Date.valueOf(fechaFinContrato));
            }
            contratoDTO.setTipoContrato(tipoContrato);

            return ResponseEntity.ok(contratoService.crear(contratoDTO, documento));
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
            @Valid @RequestBody ContratoDTO contratoDTO,
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
            return ResponseEntity.ok(contratoService.actualizar(id, contratoDTO));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 