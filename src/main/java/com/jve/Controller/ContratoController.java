package com.jve.Controller;

import com.jve.DTO.ContratoDTO;
import com.jve.Entity.TipoContrato;
import com.jve.Service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
            return ResponseEntity.ok(contratoService.obtenerTodos());
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
            return ResponseEntity.ok(contratoService.obtenerPorId(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPorUsuarioId(@PathVariable Integer usuarioId) {
        try {
            return ResponseEntity.ok(contratoService.obtenerPorUsuarioId(usuarioId));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(
            @RequestParam("usuarioId") Integer usuarioId,
            @RequestParam("fechaInicioContrato") String fechaInicioContrato,
            @RequestParam(value = "fechaFinContrato", required = false) String fechaFinContrato,
            @RequestParam("tipoContrato") TipoContrato tipoContrato,
            @RequestParam("documentoContrato") MultipartFile documento,
            @RequestParam("salario") BigDecimal salario) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(contratoService.crear(usuarioId, fechaInicioContrato, fechaFinContrato, tipoContrato.toString(), documento, salario));
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
            response.put("mensaje", "Error de validación");
            response.put("errores", result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage()
                )));
            return ResponseEntity.badRequest().body(response);
        }

        try {
            return ResponseEntity.ok(contratoService.actualizar(id, contratoDTO));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/usuario/{usuarioId}/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> descargarPDF(@PathVariable Integer usuarioId) {
        try {
            Resource resource = contratoService.descargarPDF(usuarioId);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contrato.pdf\"")
                .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/trabajador/{id}/activo")
    public ResponseEntity<Map<String, Object>> verificarContratoActivo(@PathVariable Integer id) {
        try {
            Map<String, Object> response = contratoService.verificarContratoActivo(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 