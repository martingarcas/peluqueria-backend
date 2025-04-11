package com.jve.Controller;

import com.jve.Entity.Estado;
import com.jve.Service.EstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;

    @GetMapping
    public ResponseEntity<Map<String, List<Estado>>> obtenerEstadosPorTipo(
            @RequestParam(required = true) String tipo) {
        return ResponseEntity.ok(estadoService.obtenerPorTipo(tipo));
    }
} 