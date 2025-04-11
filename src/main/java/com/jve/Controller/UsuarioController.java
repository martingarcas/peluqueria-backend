package com.jve.Controller;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Service.UsuarioService;
import com.jve.Converter.UsuarioConverter;
import com.jve.Entity.RolUsuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    public ResponseEntity<Map<String, Object>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUsuarioById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(usuarioService.getUsuarioById(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUsuario(@PathVariable Integer id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            return ResponseEntity.ok(usuarioService.updateUsuario(id, usuarioDTO));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUsuario(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(usuarioService.deleteUsuario(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/trabajadores")
    public ResponseEntity<Map<String, Object>> getAllTrabajadores() {
        Map<String, Object> response = usuarioService.getAllUsuarios();
        @SuppressWarnings("unchecked")
        List<RegistroResponseDTO> usuarios = (List<RegistroResponseDTO>) response.get("usuarios");
        
        List<RegistroResponseDTO> trabajadores = usuarios.stream()
            .filter(u -> u.getRole().equals(RolUsuario.trabajador.name()))
            .collect(Collectors.toList());
        
        response.put("usuarios", trabajadores);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/administradores")
    public ResponseEntity<Map<String, Object>> getAllAdministradores() {
        Map<String, Object> response = usuarioService.getAllUsuarios();
        @SuppressWarnings("unchecked")
        List<RegistroResponseDTO> usuarios = (List<RegistroResponseDTO>) response.get("usuarios");
        
        List<RegistroResponseDTO> administradores = usuarios.stream()
            .filter(u -> u.getRole().equals(RolUsuario.admin.name()))
            .collect(Collectors.toList());
        
        response.put("usuarios", administradores);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clientes")
    public ResponseEntity<Map<String, Object>> getAllClientes() {
        Map<String, Object> response = usuarioService.getAllUsuarios();
        @SuppressWarnings("unchecked")
        List<RegistroResponseDTO> usuarios = (List<RegistroResponseDTO>) response.get("usuarios");
        
        List<RegistroResponseDTO> clientes = usuarios.stream()
            .filter(u -> u.getRole().equals(RolUsuario.cliente.name()))
            .collect(Collectors.toList());
        
        response.put("usuarios", clientes);
        return ResponseEntity.ok(response);
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