package com.jve.Controller;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.ContratoDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Service.UsuarioService;
import com.jve.Converter.UsuarioConverter;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.TipoContrato;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Entity.Usuario;
import com.jve.Repository.UsuarioRepository;
import com.jve.Entity.Producto;
import com.jve.Repository.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioConverter usuarioConverter;
    private final String UPLOAD_DIR = "uploads/users/";
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsuarios() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(usuarioService.getAllUsuarios());
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUsuarioById(@PathVariable Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();
            boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !usuarioService.isOwnProfile(id, userEmail)) {
                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", ValidationErrorMessages.AUTH_NO_PERMISOS);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Map<String, Object> response = usuarioService.getUsuarioById(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUsuario(
            @Valid @RequestBody UsuarioDTO usuarioDTO,
            BindingResult result) {
        
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage()
                ));
            response.put("mensaje", ValidationErrorMessages.ERROR_VALIDACION);
            response.put("errores", errores);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (usuarioDTO.getRole() != null && 
            RolUsuario.trabajador.name().equalsIgnoreCase(usuarioDTO.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Para crear un trabajador use el endpoint /api/usuarios/trabajador");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Map<String, Object> response = usuarioService.createUsuario(usuarioDTO, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Establecer el rol y documentos
        usuarioDTO.setRole(RolUsuario.trabajador.name());
        usuarioDTO.setDocumentoContrato(documentoContrato);

        try {
            response = usuarioService.createUsuario(usuarioDTO, foto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            return ResponseEntity.status(HttpStatus.OK).body(updateResponse);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_MODIFIED) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            throw ex;
        } catch (RuntimeException e) {
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
            return ResponseEntity.status(HttpStatus.OK).body(usuarioService.deleteUsuario(id, isAdmin));
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<Map<String, Object>> uploadFoto(
            @PathVariable Integer id, 
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = usuarioService.updateFoto(id, file);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Foto actualizada correctamente");
            response.put("url", fileUrl);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al subir la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<?> getFoto(@PathVariable Integer id) {
        try {
            byte[] image = usuarioService.getFotoBytes(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al obtener la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}/foto")
    public ResponseEntity<Map<String, Object>> deleteFoto(@PathVariable Integer id) {
        try {
            usuarioService.deleteFoto(id);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Foto eliminada correctamente");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al eliminar la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping(value = "/imagen/{filename:.+}")
    public ResponseEntity<?> getImage(@PathVariable String filename) {
        try {
            byte[] imageBytes = usuarioService.getImageBytes(filename);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al obtener la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/carrito")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> obtenerCarrito() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        // Si el carrito es null, inicializarlo como array vacío
        if (usuario.getCarrito() == null) {
            usuario.setCarrito("[]");
            usuarioRepository.save(usuario);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Carrito recuperado exitosamente");
        response.put("carrito", usuario.getCarrito());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/carrito")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> actualizarCarrito(@RequestBody List<Map<String, Object>> nuevosProdutos) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        if (!usuario.getRol().equals(RolUsuario.cliente)) {
            throw new RuntimeException("Solo los clientes pueden modificar el carrito");
        }

        try {
            // Obtener el carrito actual
            List<Map<String, Object>> carritoActual = new ArrayList<>();
            if (usuario.getCarrito() != null && !usuario.getCarrito().equals("[]")) {
                carritoActual = objectMapper.readValue(usuario.getCarrito(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            }

            // Por cada nuevo producto
            for (Map<String, Object> nuevoItem : nuevosProdutos) {
                // Validar que solo vengan los campos permitidos
                Set<String> camposPermitidos = Set.of("productoId", "cantidad");
                Set<String> camposRecibidos = nuevoItem.keySet();
                
                Set<String> camposNoPermitidos = new HashSet<>(camposRecibidos);
                camposNoPermitidos.removeAll(camposPermitidos);
                
                if (!camposNoPermitidos.isEmpty()) {
                    throw new RuntimeException("Campos no permitidos en el carrito: " + String.join(", ", camposNoPermitidos) + 
                                            ". Solo se permiten: productoId y cantidad");
                }

                // Validar que el item tenga los campos necesarios
                if (!nuevoItem.containsKey("productoId") || !nuevoItem.containsKey("cantidad")) {
                    throw new RuntimeException("Cada item del carrito debe tener productoId y cantidad");
                }

                Integer productoId = ((Number) nuevoItem.get("productoId")).intValue();
                Integer cantidad = ((Number) nuevoItem.get("cantidad")).intValue();

                // Validar que el producto existe
                Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

                // Buscar si el producto ya existe en el carrito
                boolean productoEncontrado = false;
                Iterator<Map<String, Object>> iterator = carritoActual.iterator();
                
                while (iterator.hasNext()) {
                    Map<String, Object> itemExistente = iterator.next();
                    if (itemExistente.get("productoId").equals(productoId)) {
                        productoEncontrado = true;
                        int cantidadActual = ((Number) itemExistente.get("cantidad")).intValue();
                        
                        if (cantidad == 0) {
                            // Si la cantidad es 0, eliminar el producto
                            iterator.remove();
                        } else if (cantidad < 0) {
                            // Si la cantidad es negativa, restar del total
                            int cantidadARestar = Math.abs(cantidad);
                            int nuevaCantidad = cantidadActual - cantidadARestar;
                            
                            if (nuevaCantidad <= 0) {
                                // Si la nueva cantidad es 0 o negativa, eliminar el producto
                                iterator.remove();
                            } else {
                                // Actualizar la cantidad
                                itemExistente.put("cantidad", nuevaCantidad);
                            }
                        } else {
                            // Si la cantidad es positiva, sumar al total
                            int cantidadTotal = cantidadActual + cantidad;
                            
                            // Validar stock para la cantidad total
                            if (producto.getStock() < cantidadTotal) {
                                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                            }
                            
                            itemExistente.put("cantidad", cantidadTotal);
                        }
                        break;
                    }
                }

                // Si el producto no existía y la cantidad es positiva, añadirlo al carrito
                if (!productoEncontrado && cantidad > 0) {
                    // Validar stock
                    if (producto.getStock() < cantidad) {
                        throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                    }

                    Map<String, Object> nuevoItemValidado = new HashMap<>();
                    nuevoItemValidado.put("productoId", productoId);
                    nuevoItemValidado.put("nombreProducto", producto.getNombre());
                    nuevoItemValidado.put("cantidad", cantidad);
                    nuevoItemValidado.put("precioUnitario", producto.getPrecio());
                    carritoActual.add(nuevoItemValidado);
                }
            }

            // Convertir a JSON sin formato
            String carritoJson = objectMapper.writeValueAsString(carritoActual);
            
            // Actualizar carrito del usuario
            usuario.setCarrito(carritoJson);
            usuarioRepository.save(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Carrito actualizado exitosamente");
            response.put("carrito", carritoJson);
            return ResponseEntity.status(HttpStatus.OK).body(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Error al actualizar el carrito: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/carrito")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Map<String, Object>> vaciarCarrito() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        if (!usuario.getRol().equals(RolUsuario.cliente)) {
            throw new RuntimeException("Solo los clientes pueden vaciar el carrito");
        }

        usuario.setCarrito("[]");
        usuarioRepository.save(usuario);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Carrito vaciado exitosamente");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
} 