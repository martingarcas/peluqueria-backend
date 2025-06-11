package com.jve.Service;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.ContratoDTO;
import com.jve.Entity.Usuario;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.Servicio;
import com.jve.Entity.Horario;
import com.jve.Entity.Producto;
import com.jve.Entity.TipoContrato;
import com.jve.Repository.UsuarioRepository;
import com.jve.Repository.ServicioRepository;
import com.jve.Repository.HorarioRepository;
import com.jve.Repository.ProductoRepository;
import com.jve.Repository.ContratoRepository;
import com.jve.Converter.UsuarioConverter;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import com.jve.Exception.ResourceNotFoundException;
import com.jve.Exception.ResourceAlreadyExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final ServicioRepository servicioRepository;
    private final HorarioRepository horarioRepository;
    private final ProductoRepository productoRepository;
    private final ContratoService contratoService;
    private final ContratoRepository contratoRepository;
    private final ObjectMapper objectMapper;
    private final String UPLOAD_DIR = "uploads/";
    private final String UPLOAD_DIR_FOTOS = "uploads/users/fotos/";

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTodos() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIOS_LISTADOS);
        response.put("usuarios", usuarios.stream()
            .map(usuarioConverter::toResponseDTO)
            .collect(Collectors.toList()));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPorRol(String rolNombre) {
        try {
            RolUsuario rol = RolUsuario.valueOf(rolNombre.toLowerCase());
            List<Usuario> usuarios = usuarioRepository.findAll()
                .stream()
                .filter(u -> u.getRol() == rol)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ResponseMessages.USUARIOS_LISTADOS);
            response.put("usuarios", usuarios.stream()
                .map(usuarioConverter::toResponseDTO)
                .collect(Collectors.toList()));
            return response;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol no válido: " + rolNombre);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.ENTIDAD_RECUPERADA);
        response.put("usuario", usuarioConverter.toResponseDTO(usuario));
        return response;
    }

    @Transactional
    public Map<String, Object> crear(UsuarioDTO usuarioDTO, MultipartFile foto, MultipartFile documentoContrato) {
        // Validar si ya existe un usuario con el mismo email
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
        }

        // Validar que el rol sea válido
        if (usuarioDTO.getRole() == null || !isValidRole(usuarioDTO.getRole())) {
            throw new IllegalArgumentException("El rol especificado no es válido. Roles válidos: cliente, trabajador, admin");
        }

        // Si es trabajador, validar datos adicionales
        if (usuarioDTO.getRole() != null && 
            RolUsuario.trabajador.name().equalsIgnoreCase(usuarioDTO.getRole())) {
            
            // Validar que tenga servicios y horarios asignados
            if (usuarioDTO.getServiciosIds() == null || usuarioDTO.getServiciosIds().isEmpty()) {
                throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_SERVICIOS_REQUERIDOS);
            }
            if (usuarioDTO.getHorariosIds() == null || usuarioDTO.getHorariosIds().isEmpty()) {
                throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_HORARIOS_REQUERIDOS);
            }

            // Validar que el documento del contrato esté presente
            if (documentoContrato == null || documentoContrato.isEmpty()) {
                throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_CONTRATO_REQUERIDO);
            }

            // Validar que los servicios y horarios existan
            try {
                validarServiciosExisten(usuarioDTO.getServiciosIds());
                validarHorariosExisten(usuarioDTO.getHorariosIds());
            } catch (RuntimeException e) {
                throw new RuntimeException("Error validando servicios/horarios: " + e.getMessage());
            }

            // Asignar el documento al DTO para procesarlo
            usuarioDTO.setDocumentoContrato(documentoContrato);
        }

        // Crear el usuario
        Usuario usuario = usuarioConverter.toEntity(usuarioDTO);
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setFechaRegistro(new Date());

        // Guardar foto si existe
        if (foto != null && !foto.isEmpty()) {
            try {
                // Crear el directorio si no existe
                Path uploadPath = Paths.get(UPLOAD_DIR_FOTOS);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generar nombre único para el archivo
                String nombreArchivo = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                Path filePath = uploadPath.resolve(nombreArchivo);

                // Guardar archivo
                Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Actualizar URL de la foto
                usuario.setFoto("/uploads/users/fotos/" + nombreArchivo);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la foto del usuario", e);
            }
        }

        // Guardar el usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Si el usuario es un trabajador, gestionar datos adicionales
        if (usuarioDTO.getRole() != null && 
            RolUsuario.trabajador.name().equalsIgnoreCase(usuarioDTO.getRole())) {
            try {
                gestionarDatosTrabajador(usuarioGuardado, usuarioDTO);
            } catch (Exception e) {
                // Si hay error al gestionar datos del trabajador, revertir todo
                usuarioRepository.delete(usuarioGuardado);
                if (usuario.getFoto() != null) {
                    try {
                        Path fotoPath = Paths.get(usuario.getFoto().replace("/uploads/", "uploads/"));
                        Files.deleteIfExists(fotoPath);
                    } catch (IOException ioEx) {
                        // Log error pero continuar con el rollback
                        System.err.println("Error eliminando foto: " + ioEx.getMessage());
                    }
                }
                throw new RuntimeException("Error al gestionar datos del trabajador: " + e.getMessage());
            }
        }

        // Preparar la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_CREADO);
        response.put("usuario", usuarioConverter.toResponseDTO(usuarioGuardado));
        return response;
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, UsuarioDTO usuarioDTO, MultipartFile foto, MultipartFile documentoContrato) {
        try {
            Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

            // Validar que el email no exista si se está cambiando
            if (!usuario.getEmail().equals(usuarioDTO.getEmail()) && 
                usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
                throw new ResourceAlreadyExistsException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
            }

            // Actualizar datos básicos
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setApellidos(usuarioDTO.getApellidos());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setDireccion(usuarioDTO.getDireccion());
            usuario.setTelefono(usuarioDTO.getTelefono());

            // Si es un trabajador, actualizar servicios y horarios
            if (usuario.getRol() == RolUsuario.trabajador) {
                // Validar y actualizar servicios
                if (usuarioDTO.getServiciosIds() != null && !usuarioDTO.getServiciosIds().isEmpty()) {
                    List<Servicio> servicios = servicioRepository.findAllById(usuarioDTO.getServiciosIds());
                    if (servicios.size() != usuarioDTO.getServiciosIds().size()) {
                        throw new RuntimeException(ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
                    }
                    usuario.setServicios(servicios);
                }

                // Validar y actualizar horarios
                if (usuarioDTO.getHorariosIds() != null && !usuarioDTO.getHorariosIds().isEmpty()) {
                    List<Horario> horarios = horarioRepository.findAllById(usuarioDTO.getHorariosIds());
                    if (horarios.size() != usuarioDTO.getHorariosIds().size()) {
                        throw new RuntimeException(ValidationErrorMessages.HORARIOS_NO_ENCONTRADOS);
                    }
                    usuario.setHorarios(horarios);
                }

                // Manejar el contrato si se proporciona uno nuevo
                if (usuarioDTO.getContrato() != null) {
                    // Verificar si tiene un contrato activo o pendiente
                    boolean tieneContratoActivoOPendiente = contratoRepository.existsByUsuarioIdAndEstadoNombreIn(
                        usuario.getId(), 
                        Arrays.asList("ACTIVO", "PENDIENTE")
                    );

                    if (!tieneContratoActivoOPendiente) {
                        // Si no tiene contrato activo o pendiente, crear uno nuevo
                        ContratoDTO contratoDTO = usuarioDTO.getContrato();
                        contratoDTO.setUsuarioId(usuario.getId());

                        // Si se proporciona el documento del contrato, usarlo
                        if (documentoContrato != null && !documentoContrato.isEmpty()) {
                            contratoService.crear(
                                usuario.getId(),
                                contratoDTO,
                                documentoContrato
                            );
                        } else {
                            // Si no se proporciona el documento, crear el contrato sin él
                            contratoService.crear(
                                usuario.getId(),
                                contratoDTO,
                                null
                            );
                        }
                    } else {
                        System.out.println("No se crea nuevo contrato porque ya tiene uno activo o pendiente");
                    }
                }
            }

            // Actualizar contraseña solo si se proporciona y cumple el formato
            String password = usuarioDTO.getPassword();
            if (password != null && !password.trim().isEmpty()) {
                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
                    Map<String, Object> response = new HashMap<>();
                    Map<String, String> errores = new HashMap<>();
                    errores.put("password", ValidationErrorMessages.AUTH_PASSWORD_FORMATO);
                    response.put("mensaje", "Error de validación");
                    response.put("errores", errores);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error de validación", 
                        new RuntimeException(objectMapper.writeValueAsString(response)));
                }
                usuario.setPassword(passwordEncoder.encode(password));
            }

            // Actualizar foto si existe
            if (foto != null && !foto.isEmpty()) {
                try {
                    // Eliminar foto anterior si existe
                    if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
                        Path fotoAnterior = Paths.get(usuario.getFoto().replace("/uploads/", UPLOAD_DIR));
                        Files.deleteIfExists(fotoAnterior);
                    }

                    // Crear el directorio si no existe
                    Path uploadPath = Paths.get(UPLOAD_DIR_FOTOS);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // Generar nombre único para el archivo
                    String nombreArchivo = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                    Path filePath = uploadPath.resolve(nombreArchivo);

                    // Guardar archivo
                    Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // Actualizar URL de la foto
                    usuario.setFoto("/uploads/users/fotos/" + nombreArchivo);
                } catch (IOException e) {
                    throw new RuntimeException("Error al guardar la foto del usuario", e);
                }
            }

            usuario = usuarioRepository.save(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ResponseMessages.USUARIO_ACTUALIZADO);
            response.put("usuario", usuarioConverter.toResponseDTO(usuario));
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al procesar la validación", e);
        }
    }

    @Transactional
    public Map<String, Object> eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        // Eliminar foto si existe
        if (usuario.getFoto() != null && !usuario.getFoto().isEmpty()) {
            try {
                Path fotoPath = Paths.get(usuario.getFoto());
                Files.deleteIfExists(fotoPath);
            } catch (IOException e) {
                // Solo loguear el error, no interrumpir la eliminación
                System.err.println("Error al eliminar la foto: " + e.getMessage());
            }
        }
        
        usuarioRepository.deleteById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_ELIMINADO);
        return response;
    }

    private void gestionarDatosTrabajador(Usuario usuario, UsuarioDTO usuarioDTO) {
        try {

            contratoService.crear(
                usuario.getId(),
                usuarioDTO.getContrato(),
                usuarioDTO.getDocumentoContrato()
            );

            List<Servicio> servicios = servicioRepository.findAllById(usuarioDTO.getServiciosIds());
            if (servicios.size() != usuarioDTO.getServiciosIds().size()) {
                throw new RuntimeException(ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
            }
            usuario.setServicios(servicios);

            List<Horario> horarios = horarioRepository.findAllById(usuarioDTO.getHorariosIds());
            if (horarios.size() != usuarioDTO.getHorariosIds().size()) {
                throw new RuntimeException(ValidationErrorMessages.HORARIOS_NO_ENCONTRADOS);
            }
            usuario.setHorarios(horarios);

            usuarioRepository.save(usuario);
        } catch (Exception e) {
            System.out.println("=== ERROR EN gestionarDatosTrabajador ===");
            System.out.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al gestionar datos del trabajador: " + e.getMessage());
        }
    }

    public void validarServiciosExisten(List<Integer> serviciosIds) {
        List<Servicio> serviciosEncontrados = servicioRepository.findAllById(serviciosIds);
        if (serviciosEncontrados.size() != serviciosIds.size()) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
        }
    }

    public void validarHorariosExisten(List<Integer> horariosIds) {
        List<Horario> horariosEncontrados = horarioRepository.findAllById(horariosIds);
        if (horariosEncontrados.size() != horariosIds.size()) {
            throw new RuntimeException(ValidationErrorMessages.HORARIOS_NO_ENCONTRADOS);
        }
    }

    public Map<String, String> validarDatosTrabajador(
            UsuarioDTO usuarioDTO, 
            MultipartFile foto, 
            MultipartFile documentoContrato,
            String fechaInicioContrato,
            String fechaFinContrato) {
        
        Map<String, String> errores = new HashMap<>();

        // Validar campos básicos del usuario
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

        // La dirección ya no es obligatoria, solo validamos el formato si se proporciona
        if (usuarioDTO.getDireccion() != null && !usuarioDTO.getDireccion().trim().isEmpty() && 
            (usuarioDTO.getDireccion().length() < 5 || usuarioDTO.getDireccion().length() > 200)) {
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

        if (documentoContrato == null || documentoContrato.isEmpty()) {
            errores.put("documentoContrato", ValidationErrorMessages.TRABAJADOR_CONTRATO_REQUERIDO);
        }

        // Validar fechas del contrato
        if (fechaInicioContrato == null || fechaInicioContrato.trim().isEmpty()) {
            errores.put("fechaInicioContrato", ValidationErrorMessages.CONTRATO_FECHA_INICIO_REQUERIDA);
        } else {
            try {
                java.sql.Date.valueOf(fechaInicioContrato);
            } catch (IllegalArgumentException e) {
                errores.put("fechaInicioContrato", "El formato de la fecha debe ser YYYY-MM-DD");
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
                validarServiciosExisten(serviciosIds);
            } catch (RuntimeException e) {
                errores.put("serviciosIds", ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
            }
        }

        if (horariosIds == null || horariosIds.isEmpty()) {
            errores.put("horariosIds", ValidationErrorMessages.TRABAJADOR_HORARIOS_REQUERIDOS);
        } else {
            try {
                validarHorariosExisten(horariosIds);
            } catch (RuntimeException e) {
                errores.put("horariosIds", ValidationErrorMessages.HORARIOS_NO_ENCONTRADOS);
            }
        }

        return errores;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerCarrito() {
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
        return response;
    }

    @Transactional
    public Map<String, Object> actualizarCarrito(List<Map<String, Object>> nuevosProdutos) {
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
                // Validar campos obligatorios
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
                    // Si encontramos el producto en el carrito
                    if (itemExistente.get("productoId").equals(productoId)) {
                        productoEncontrado = true;
                        int cantidadActual = ((Number) itemExistente.get("cantidad")).intValue();
                        // CASO 1: Si la cantidad nueva es 0 o Si la cantidad nueva es negativa
                        if (cantidad <= 0) {
                            iterator.remove();// Eliminamos el producto del carrito
                        // CASO 3: Si la cantidad nueva es positiva
                        } else {
                            int cantidadTotal = cantidadActual + cantidad; //aumentamos la cantidad con la actual y la que viene
                            if (producto.getStock() < cantidadTotal) {
                                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre()); // Verificamos stock
                            }
                            itemExistente.put("cantidad", cantidadTotal);
                        }
                        break; // Salimos del bucle porque ya encontramos el producto
                    }
                }

                // Si el producto no existía y la cantidad es positiva, añadirlo al carrito
                if (!productoEncontrado && cantidad > 0) {
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
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el carrito: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> vaciarCarrito() {
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
        return response;
    }

    private boolean isValidRole(String role) {
        try {
            RolUsuario.valueOf(role.toLowerCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 