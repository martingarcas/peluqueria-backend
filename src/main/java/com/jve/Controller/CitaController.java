package com.jve.Controller;

import com.jve.DTO.CitaDTO;
import com.jve.Service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {
    
    private final CitaService citaService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerCitasUsuario() {
        try {
            Map<String, Object> response = citaService.obtenerCitasPorUsuario();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> crearCita(
            @Valid @RequestBody CitaDTO citaDTO,
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            Map<String, Object> response = citaService.crearCita(citaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<Map<String, Object>> actualizarEstadoCita(
            @PathVariable Integer id,
            @RequestParam String estado) {
        try {
            Map<String, Object> response = citaService.actualizarEstadoCita(id, estado);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> obtenerDisponibilidad(
            @RequestParam Integer trabajadorId,
            @RequestParam(required = false) Integer servicioId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fecha) {
        try {
            Map<String, Object> response = citaService.obtenerDisponibilidadTrabajador(trabajadorId, servicioId, fecha);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/trabajadores-no-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerTrabajadoresNoDisponibles(
            @RequestParam Integer servicioId,
            @RequestParam String fecha,
            @RequestParam String hora) {
        try {
            Map<String, Object> response = citaService.obtenerTrabajadoresNoDisponiblesConValidacion(servicioId, fecha, hora);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ParseException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "El formato de fecha debe ser yyyy-MM-dd");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/dias-no-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerDiasNoDisponibles(
            @RequestParam Integer servicioId,
            @RequestParam String hora,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            Map<String, Object> response = citaService.obtenerDiasNoDisponiblesConValidacion(
                servicioId, hora, fechaInicio, fechaFin);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (ParseException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "El formato de fecha debe ser yyyy-MM-dd");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/trabajadores-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerTrabajadoresDisponibles(
            @RequestParam Integer servicioId) {
        try {
            Map<String, Object> response = citaService.obtenerTrabajadoresDisponibles(servicioId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerCitaPorId(@PathVariable Integer id) {
        try {
            Map<String, Object> response = citaService.obtenerCitaPorId(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{id}/reasignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reasignarCita(
            @PathVariable Integer id,
            @Valid @RequestBody CitaDTO.ReasignacionRequest reasignacionRequest,
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Map<String, Object> response = citaService.reasignarCita(id, reasignacionRequest);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerCitasCliente(@PathVariable Integer id) {
        try {
            Map<String, Object> response = citaService.obtenerCitasPorCliente(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/trabajador/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerCitasTrabajador(@PathVariable Integer id) {
        try {
            Map<String, Object> response = citaService.obtenerCitasPorTrabajador(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 