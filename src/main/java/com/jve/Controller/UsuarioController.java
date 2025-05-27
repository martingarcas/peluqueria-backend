package com.jve.Controller;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.ContratoDTO;
import com.jve.Service.UsuarioService;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.TipoContrato;
import com.jve.Exception.ValidationErrorMessages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerTodos() {
        try {
            Map<String, Object> response = usuarioService.obtenerTodos();
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
            Map<String, Object> response = usuarioService.obtenerPorId(id);
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
            @Valid @RequestPart("usuario") UsuarioDTO usuarioDTO,
            @RequestPart("foto") MultipartFile foto,
            @RequestPart(value = "documentoContrato", required = false) MultipartFile documentoContrato) {
        try {
            Map<String, Object> response = usuarioService.crear(usuarioDTO, foto, documentoContrato);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable Integer id,
            @RequestPart("usuario") UsuarioDTO usuarioDTO,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestPart(value = "documentoContrato", required = false) MultipartFile documentoContrato) {
        usuarioDTO.setId(id);
        return ResponseEntity.ok(usuarioService.actualizar(id, usuarioDTO, foto, documentoContrato));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Integer id) {
        try {
            Map<String, Object> response = usuarioService.eliminar(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> actualizarParcial(
            @PathVariable Integer id,
            @RequestPart("usuario") UsuarioDTO usuarioDTO,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        usuarioDTO.setId(id);
        return ResponseEntity.ok(usuarioService.actualizarParcial(id, usuarioDTO, foto));
    }

    // Endpoint específico para crear trabajadores con toda la información necesaria
    @PostMapping(value = "/trabajador", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crearTrabajador(
            @RequestPart(value = "usuario") UsuarioDTO usuarioDTO,
            @RequestPart(value = "foto") MultipartFile foto,
            @RequestPart(value = "documentoContrato") MultipartFile documentoContrato,
            @RequestParam(value = "fechaInicioContrato") String fechaInicioContrato,
            @RequestParam(value = "fechaFinContrato", required = false) String fechaFinContrato,
            @RequestParam(value = "tipoContrato") String tipoContrato,
            @RequestParam(value = "salario") String salario) {
        try {
            Map<String, Object> response = usuarioService.crearTrabajador(
                usuarioDTO, foto, documentoContrato, 
                fechaInicioContrato, fechaFinContrato, 
                tipoContrato, salario
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/trabajadores")
    public ResponseEntity<Map<String, Object>> obtenerTrabajadores() {
        try {
            Map<String, Object> response = usuarioService.obtenerPorRol("trabajador");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/clientes")
    public ResponseEntity<Map<String, Object>> obtenerClientes() {
        try {
            Map<String, Object> response = usuarioService.obtenerPorRol("cliente");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/carrito")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> obtenerCarrito() {
        try {
            Map<String, Object> response = usuarioService.obtenerCarrito();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/carrito")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> actualizarCarrito(@RequestBody List<Map<String, Object>> nuevosProdutos) {
        try {
            Map<String, Object> response = usuarioService.actualizarCarrito(nuevosProdutos);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/carrito")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> vaciarCarrito() {
        try {
            Map<String, Object> response = usuarioService.vaciarCarrito();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 