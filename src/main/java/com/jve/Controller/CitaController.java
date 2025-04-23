package com.jve.Controller;

import com.jve.DTO.CitaDTO;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.sql.Time;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {
    
    private final CitaService citaService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerCitasUsuario() {
        return ResponseEntity.ok(citaService.obtenerCitasPorUsuario());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> crearCita(
            @Valid @RequestBody CitaDTO citaDTO,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ValidationErrorMessages.ERROR_VALIDACION);
            response.put("errores", bindingResult.getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.toList()));
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(citaService.crearCita(citaDTO));
    }
    
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<Map<String, Object>> actualizarEstadoCita(
            @PathVariable Integer id,
            @RequestParam String estado) {
        return ResponseEntity.ok(citaService.actualizarEstadoCita(id, estado));
    }
    
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> obtenerDisponibilidad(
            @RequestParam Integer trabajadorId,
            @RequestParam(required = false) Integer servicioId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fecha) {
        return ResponseEntity.ok(citaService.obtenerDisponibilidadTrabajador(trabajadorId, servicioId, fecha));
    }
    
    @GetMapping("/trabajadores-no-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerTrabajadoresNoDisponibles(
            @RequestBody Map<String, Object> request) {
        Integer servicioId = Integer.parseInt(request.get("servicioId").toString());
        String fechaStr = request.get("fecha").toString();
        String hora = request.get("hora").toString();
        
        try {
            return ResponseEntity.ok(citaService.obtenerTrabajadoresNoDisponiblesConValidacion(servicioId, fechaStr, hora));
        } catch (ParseException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "El formato de fecha debe ser yyyy-MM-dd");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/dias-no-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerDiasNoDisponibles(
            @RequestBody Map<String, Object> request) {
        Integer servicioId = Integer.parseInt(request.get("servicioId").toString());
        String hora = request.get("hora").toString();
        String fechaInicioStr = request.get("fechaInicio").toString();
        String fechaFinStr = request.get("fechaFin").toString();
        
        try {
            return ResponseEntity.ok(citaService.obtenerDiasNoDisponiblesConValidacion(
                servicioId, hora, fechaInicioStr, fechaFinStr));
        } catch (ParseException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "El formato de fecha debe ser yyyy-MM-dd");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/trabajadores-disponibles")
    public ResponseEntity<Map<String, Object>> obtenerTrabajadoresDisponibles(
            @RequestParam Integer servicioId) {
        return ResponseEntity.ok(citaService.obtenerTrabajadoresDisponibles(servicioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerCitaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    @PutMapping("/{id}/reasignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reasignarCita(
            @PathVariable Integer id,
            @Valid @RequestBody CitaDTO.ReasignacionRequest reasignacionRequest) {
        return ResponseEntity.ok(citaService.reasignarCita(id, reasignacionRequest));
    }

    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerCitasCliente(@PathVariable Integer id) {
        return ResponseEntity.ok(citaService.obtenerCitasPorCliente(id));
    }

    @GetMapping("/trabajador/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerCitasTrabajador(@PathVariable Integer id) {
        return ResponseEntity.ok(citaService.obtenerCitasPorTrabajador(id));
    }
} 