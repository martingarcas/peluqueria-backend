package com.jve.Controller;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.ContratoDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Service.UsuarioService;
import com.jve.Converter.UsuarioConverter;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.TipoContrato;
import com.jve.Exception.ValidationErrorMessages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioConverter usuarioConverter;
    private final String UPLOAD_DIR = "uploads/users/";

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsuarios() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUsuarioById(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Verificar permisos
        if (!isAdmin && !usuarioService.isOwnProfile(id, userEmail)) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "No tienes permisos para ver este perfil");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            return ResponseEntity.ok(usuarioService.getUsuarioById(id));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errores.put(error.getField(), error.getDefaultMessage())
            );
            response.put("mensaje", "Error de validación");
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        // Validar que no se intente crear un trabajador sin los archivos necesarios
        if (usuarioDTO.getRole() != null && 
            RolUsuario.trabajador.name().equalsIgnoreCase(usuarioDTO.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Para crear un trabajador use el endpoint /api/usuarios/trabajador");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Map<String, Object> response = usuarioService.createUsuario(usuarioDTO, null);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/trabajador", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTrabajador(
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @RequestPart(value = "contrato.documentoContrato", required = false) MultipartFile documentoContrato,
            @RequestParam(value = "contrato.fechaInicioContrato", required = false) String fechaInicioContrato,
            @RequestParam(value = "contrato.fechaFinContrato", required = false) String fechaFinContrato,
            @ModelAttribute UsuarioDTO usuarioDTO,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errores = new HashMap<>();

        // Validar manualmente los campos requeridos
        if (usuarioDTO.getNombre() == null || usuarioDTO.getNombre().trim().isEmpty()) {
            errores.put("nombre", ValidationErrorMessages.AUTH_NOMBRE_REQUERIDO);
        } else if (usuarioDTO.getNombre().length() < 2 || usuarioDTO.getNombre().length() > 50) {
            errores.put("nombre", "El nombre debe tener entre 2 y 50 caracteres");
        }

        if (usuarioDTO.getApellidos() == null || usuarioDTO.getApellidos().trim().isEmpty()) {
            errores.put("apellidos", ValidationErrorMessages.AUTH_APELLIDOS_REQUERIDOS);
        } else if (usuarioDTO.getApellidos().length() < 2 || usuarioDTO.getApellidos().length() > 100) {
            errores.put("apellidos", "Los apellidos deben tener entre 2 y 100 caracteres");
        }

        if (usuarioDTO.getEmail() == null || usuarioDTO.getEmail().trim().isEmpty()) {
            errores.put("email", ValidationErrorMessages.AUTH_EMAIL_REQUERIDO);
        } else if (!usuarioDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errores.put("email", ValidationErrorMessages.AUTH_EMAIL_FORMATO);
        }

        if (usuarioDTO.getDireccion() == null || usuarioDTO.getDireccion().trim().isEmpty()) {
            errores.put("direccion", ValidationErrorMessages.AUTH_DIRECCION_REQUERIDA);
        } else if (usuarioDTO.getDireccion().length() < 5 || usuarioDTO.getDireccion().length() > 200) {
            errores.put("direccion", "La dirección debe tener entre 5 y 200 caracteres");
        }

        if (usuarioDTO.getTelefono() == null || usuarioDTO.getTelefono().trim().isEmpty()) {
            errores.put("telefono", ValidationErrorMessages.AUTH_TELEFONO_REQUERIDO);
        } else if (!usuarioDTO.getTelefono().matches("^[0-9]{9}$")) {
            errores.put("telefono", "El teléfono debe tener 9 dígitos");
        }

        if (usuarioDTO.getPassword() == null || usuarioDTO.getPassword().trim().isEmpty()) {
            errores.put("password", ValidationErrorMessages.AUTH_PASSWORD_REQUERIDO);
        } else if (!usuarioDTO.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            errores.put("password", ValidationErrorMessages.AUTH_PASSWORD_FORMATO);
        }

        // Validar archivos obligatorios
        if (foto == null || foto.isEmpty()) {
            errores.put("foto", ValidationErrorMessages.TRABAJADOR_FOTO_REQUERIDA);
        }

        // Validar contrato y sus campos
        if (documentoContrato == null || documentoContrato.isEmpty()) {
            errores.put("documentoContrato", ValidationErrorMessages.TRABAJADOR_CONTRATO_REQUERIDO);
        }

        // Procesar la fecha de inicio del contrato
        if (fechaInicioContrato == null || fechaInicioContrato.trim().isEmpty()) {
            errores.put("fechaInicioContrato", ValidationErrorMessages.CONTRATO_FECHA_INICIO_REQUERIDA);
        } else {
            try {
                java.sql.Date fechaInicio = java.sql.Date.valueOf(fechaInicioContrato);
                if (usuarioDTO.getContrato() == null) {
                    usuarioDTO.setContrato(new ContratoDTO());
                }
                usuarioDTO.getContrato().setFechaInicioContrato(fechaInicio);
            } catch (IllegalArgumentException e) {
                errores.put("fechaInicioContrato", "El formato de la fecha debe ser YYYY-MM-DD");
            }
        }

        // Procesar la fecha de fin del contrato si existe
        if (fechaFinContrato != null && !fechaFinContrato.trim().isEmpty()) {
            try {
                java.sql.Date fechaFin = java.sql.Date.valueOf(fechaFinContrato);
                if (usuarioDTO.getContrato() == null) {
                    usuarioDTO.setContrato(new ContratoDTO());
                }
                usuarioDTO.getContrato().setFechaFinContrato(fechaFin);
            } catch (IllegalArgumentException e) {
                errores.put("fechaFinContrato", "El formato de la fecha debe ser YYYY-MM-DD");
            }
        }

        // Validar campos del contrato
        if (usuarioDTO.getContrato() == null) {
            errores.put("tipoContrato", ValidationErrorMessages.CONTRATO_TIPO_REQUERIDO);
            errores.put("salario", ValidationErrorMessages.CONTRATO_SALARIO_REQUERIDO);
        } else {
            // Validar tipo de contrato
            if (usuarioDTO.getContrato().getTipoContrato() == null) {
                errores.put("tipoContrato", ValidationErrorMessages.CONTRATO_TIPO_REQUERIDO);
            }

            // Validar salario
            if (usuarioDTO.getContrato().getSalario() == null) {
                errores.put("salario", ValidationErrorMessages.CONTRATO_SALARIO_REQUERIDO);
            } else if (usuarioDTO.getContrato().getSalario().compareTo(BigDecimal.ZERO) <= 0) {
                errores.put("salario", ValidationErrorMessages.CONTRATO_SALARIO_NEGATIVO);
            }

            // Validar fecha fin para contratos temporales
            if (TipoContrato.temporal.name().equalsIgnoreCase(usuarioDTO.getContrato().getTipoContrato().toString())) {
                if (fechaFinContrato == null || fechaFinContrato.trim().isEmpty()) {
                    errores.put("fechaFinContrato", ValidationErrorMessages.CONTRATO_TEMPORAL_REQUIERE_FECHA_FIN);
                }
            }
        }

        // Validar servicios y horarios
        List<Integer> serviciosIds = usuarioDTO.getServiciosIds();
        List<Integer> horariosIds = usuarioDTO.getHorariosIds();

        if (serviciosIds == null || serviciosIds.isEmpty()) {
            errores.put("serviciosIds", ValidationErrorMessages.TRABAJADOR_SERVICIOS_REQUERIDOS);
        } else {
            try {
                // Validar que los servicios existan
                usuarioService.validarServiciosExisten(serviciosIds);
            } catch (RuntimeException e) {
                errores.put("serviciosIds", ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
            }
        }

        if (horariosIds == null || horariosIds.isEmpty()) {
            errores.put("horariosIds", ValidationErrorMessages.TRABAJADOR_HORARIOS_REQUERIDOS);
        } else {
            try {
                // Validar que los horarios existan
                usuarioService.validarHorariosExisten(horariosIds);
            } catch (RuntimeException e) {
                errores.put("horariosIds", ValidationErrorMessages.HORARIOS_NO_ENCONTRADOS);
            }
        }

        // Si hay errores, devolver respuesta con todos los errores
        if (!errores.isEmpty()) {
            response.put("mensaje", ValidationErrorMessages.ERROR_VALIDACION);
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        // Establecer el rol y documentos
        usuarioDTO.setRole(RolUsuario.trabajador.name());
        usuarioDTO.setDocumentoContrato(documentoContrato);

        try {
            response = usuarioService.createUsuario(usuarioDTO, foto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioDTO usuarioDTO,
            BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errores = new HashMap<>();

        // Validar manualmente los campos requeridos
        if (usuarioDTO.getNombre() != null && (usuarioDTO.getNombre().trim().isEmpty() || 
            usuarioDTO.getNombre().length() < 2 || usuarioDTO.getNombre().length() > 50)) {
            errores.put("nombre", "El nombre debe tener entre 2 y 50 caracteres");
        }

        if (usuarioDTO.getApellidos() != null && (usuarioDTO.getApellidos().trim().isEmpty() || 
            usuarioDTO.getApellidos().length() < 2 || usuarioDTO.getApellidos().length() > 100)) {
            errores.put("apellidos", "Los apellidos deben tener entre 2 y 100 caracteres");
        }

        if (usuarioDTO.getEmail() != null && (usuarioDTO.getEmail().trim().isEmpty() || 
            !usuarioDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$"))) {
            errores.put("email", ValidationErrorMessages.AUTH_EMAIL_FORMATO);
        }

        if (usuarioDTO.getDireccion() != null && (usuarioDTO.getDireccion().trim().isEmpty() || 
            usuarioDTO.getDireccion().length() < 5 || usuarioDTO.getDireccion().length() > 200)) {
            errores.put("direccion", "La dirección debe tener entre 5 y 200 caracteres");
        }

        if (usuarioDTO.getTelefono() != null && (usuarioDTO.getTelefono().trim().isEmpty() || 
            !usuarioDTO.getTelefono().matches("^[0-9]{9}$"))) {
            errores.put("telefono", "El teléfono debe tener 9 dígitos");
        }

        // Solo validar la contraseña si se proporciona una nueva
        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().trim().isEmpty() && 
            !usuarioDTO.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            errores.put("password", ValidationErrorMessages.AUTH_PASSWORD_FORMATO);
        }

        if (!errores.isEmpty()) {
            response.put("mensaje", "Error de validación");
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Verificar permisos
        if (!isAdmin && !usuarioService.isOwnProfile(id, userEmail)) {
            response.put("mensaje", "No tienes permisos para actualizar este perfil");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            Map<String, Object> updateResponse = usuarioService.updateUsuario(id, usuarioDTO, isAdmin);
            return ResponseEntity.ok(updateResponse);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            throw ex;
        } catch (RuntimeException e) {
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUsuario(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Verificar permisos
        if (!isAdmin && !usuarioService.isOwnProfile(id, userEmail)) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "No tienes permisos para eliminar este perfil");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            return ResponseEntity.ok(usuarioService.deleteUsuario(id, isAdmin));
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