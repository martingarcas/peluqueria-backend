package com.jve.Controller;

import com.jve.DTO.ContratoDTO;
import com.jve.Entity.TipoContrato;
import com.jve.Service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerTodos() {
        try {
            Map<String, Object> response = contratoService.obtenerTodos();
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
            Map<String, Object> response = contratoService.obtenerPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPorUsuarioId(@PathVariable Integer usuarioId) {
        try {
            Map<String, Object> response = contratoService.obtenerPorUsuarioId(usuarioId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(
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

            Map<String, Object> response = contratoService.crear(contratoDTO, documento);
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
            @Valid @RequestBody ContratoDTO contratoDTO,
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
            Map<String, Object> response = contratoService.actualizar(id, contratoDTO);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 