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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioConverter usuarioConverter;
    private final String UPLOAD_DIR = "uploads/users/";

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

    @PostMapping("/{id}/foto")
    public ResponseEntity<String> uploadFoto(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            // Eliminar foto antigua si existe
            usuarioService.deleteFoto(id);

            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para el archivo
            String fileName = id + "_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Guardar archivo
            Files.copy(file.getInputStream(), filePath);

            // Actualizar URL de la foto en el usuario
            String fileUrl = "/uploads/users/" + fileName;
            usuarioService.updateFotoUrl(id, fileUrl);

            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir la imagen: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> getFoto(@PathVariable Integer id) {
        try {
            String fotoUrl = usuarioService.getFotoUrl(id);
            if (fotoUrl == null || fotoUrl.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(fotoUrl.replace("/uploads/", ""));
            byte[] image = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<Void> deleteFoto(@PathVariable Integer id) {
        try {
            usuarioService.deleteFoto(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/imagen/{filename:.+}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            Path imagePath = Paths.get("uploads/users/" + filename);
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 