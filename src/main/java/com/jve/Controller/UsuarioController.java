package com.jve.Controller;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Service.UsuarioService;
import com.jve.Converter.UsuarioConverter;
import com.jve.Entity.RolUsuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioConverter usuarioConverter;

    @GetMapping
    public ResponseEntity<List<RegistroResponseDTO>> getAllUsuarios() {
        List<RegistroResponseDTO> usuarios = usuarioService.getAllUsuarios()
            .stream()
            .map(usuarioConverter::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistroResponseDTO> getUsuarioById(@PathVariable Integer id) {
        return usuarioService.getUsuarioById(id)
                .map(usuarioConverter::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistroResponseDTO> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioService.updateUsuario(id, usuarioDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Integer id) {
        if (usuarioService.deleteUsuario(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/trabajadores")
    public ResponseEntity<List<RegistroResponseDTO>> getAllTrabajadores() {
        List<RegistroResponseDTO> trabajadores = usuarioService.getAllUsuarios()
            .stream()
            .filter(u -> u.getRol() == RolUsuario.trabajador)
            .map(usuarioConverter::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(trabajadores);
    }

    @GetMapping("/administradores")
    public ResponseEntity<List<RegistroResponseDTO>> getAllAdministradores() {
        List<RegistroResponseDTO> administradores = usuarioService.getAllUsuarios()
            .stream()
            .filter(u -> u.getRol() == RolUsuario.admin)
            .map(usuarioConverter::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(administradores);
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<RegistroResponseDTO>> getAllClientes() {
        List<RegistroResponseDTO> clientes = usuarioService.getAllUsuarios()
            .stream()
            .filter(u -> u.getRol() == RolUsuario.cliente)
            .map(usuarioConverter::toResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }
} 