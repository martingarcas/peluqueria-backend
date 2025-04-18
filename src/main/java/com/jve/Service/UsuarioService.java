package com.jve.Service;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.Servicio;
import com.jve.Entity.Horario;
import com.jve.Repository.UsuarioRepository;
import com.jve.Repository.ServicioRepository;
import com.jve.Repository.HorarioRepository;
import com.jve.Converter.UsuarioConverter;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import com.jve.Exception.ResourceNotFoundException;
import com.jve.Exception.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final ContratoService contratoService;
    private final String UPLOAD_DIR = "uploads/users/";

    @Transactional(readOnly = true)
    public Map<String, Object> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIOS_LISTADOS);
        response.put("usuarios", usuarios.stream()
            .map(usuarioConverter::toResponseDTO)
            .collect(Collectors.toList()));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsuarioById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.ENTIDAD_RECUPERADA);
        response.put("usuario", usuarioConverter.toResponseDTO(usuario));
        return response;
    }

    @Transactional
    public Map<String, Object> createUsuario(UsuarioDTO usuarioDTO) {
        // Validar si ya existe un usuario con el mismo email
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
        }

        // Validar que el rol sea válido
        if (usuarioDTO.getRole() == null || !isValidRole(usuarioDTO.getRole())) {
            throw new IllegalArgumentException("El rol especificado no es válido. Roles válidos: cliente, trabajador, admin");
        }

        // Crear el usuario
        Usuario usuario = usuarioConverter.toEntity(usuarioDTO);
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));

        // Guardar el usuario
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Si el usuario es un trabajador, gestionar datos adicionales
        if (usuarioDTO.getRole() != null && 
            RolUsuario.trabajador.name().equalsIgnoreCase(usuarioDTO.getRole())) {
            gestionarDatosTrabajador(usuarioGuardado, usuarioDTO);
        }

        // Preparar la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_CREADO);
        response.put("usuario", usuarioConverter.toResponseDTO(usuarioGuardado));
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

    @Transactional
    public Map<String, Object> createUsuario(UsuarioDTO usuarioDTO, MultipartFile foto) {
        Map<String, Object> response = createUsuario(usuarioDTO);
        
        if (foto != null && !foto.isEmpty()) {
            try {
                // Crear el directorio si no existe
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generar nombre único para el archivo
                String nombreArchivo = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
                Path filePath = uploadPath.resolve(nombreArchivo);

                // Guardar archivo
                Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Actualizar URL de la foto
                Usuario usuario = usuarioRepository.findById(((RegistroResponseDTO)response.get("usuario")).getId())
                    .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
                usuario.setFoto(UPLOAD_DIR + "/" + nombreArchivo);
                usuarioRepository.save(usuario);
                
                response.put("usuario", usuarioConverter.toResponseDTO(usuario));
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar la foto del usuario", e);
            }
        }
        
        return response;
    }

    @Transactional
    public Map<String, Object> updateUsuario(Integer id, UsuarioDTO usuarioDTO, boolean isAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        // Si no es admin, no puede cambiar el rol
        if (!isAdmin && usuarioDTO.getRole() != null && 
            !usuario.getRol().name().equals(usuarioDTO.getRole())) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_NO_PERMISOS_ROL);
        }

        // Validar que el email no exista si se está cambiando
        if (!usuario.getEmail().equals(usuarioDTO.getEmail()) && 
            usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new ResourceAlreadyExistsException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
        }

        // Verificar si hay cambios
        if (!usuarioDTO.tieneModificaciones(usuarioConverter.toDTO(usuario))) {
            throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
        }

        // Actualizar datos básicos
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setTelefono(usuarioDTO.getTelefono());

        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }

        // Si es admin y está cambiando el rol a trabajador
        if (isAdmin && usuarioDTO.getRole() != null && 
            RolUsuario.trabajador.name().equalsIgnoreCase(usuarioDTO.getRole()) && 
            usuario.getRol() != RolUsuario.trabajador) {
            
            validarDatosTrabajador(usuarioDTO);
            usuario.setRol(RolUsuario.trabajador);
            usuario = usuarioRepository.save(usuario);
            gestionarDatosTrabajador(usuario, usuarioDTO);
        } else {
            usuario = usuarioRepository.save(usuario);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_ACTUALIZADO);
        response.put("usuario", usuarioConverter.toResponseDTO(usuario));
        return response;
    }

    @Transactional
    public Map<String, Object> deleteUsuario(Integer id, boolean isAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        // Si es trabajador y no es admin, no puede eliminar su cuenta
        if (usuario.getRol() == RolUsuario.trabajador && !isAdmin) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_NO_PERMISOS_ELIMINAR);
        }
        
        // Eliminar foto si existe
        deleteFoto(id);
        
        usuarioRepository.deleteById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_ELIMINADO);
        return response;
    }

    @Transactional(readOnly = true)
    public boolean isOwnProfile(Integer id, String email) {
        return usuarioRepository.findById(id)
            .map(usuario -> usuario.getEmail().equals(email))
            .orElse(false);
    }

    private void validarDatosTrabajador(UsuarioDTO usuarioDTO) {
        if (usuarioDTO.getContrato() == null) {
            throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_SIN_CONTRATO);
        }
        if (usuarioDTO.getServiciosIds() == null || usuarioDTO.getServiciosIds().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_SIN_SERVICIOS);
        }
        if (usuarioDTO.getHorariosIds() == null || usuarioDTO.getHorariosIds().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.TRABAJADOR_SIN_HORARIOS);
        }

        // Validar que los servicios existan
        List<Servicio> servicios = servicioRepository.findAllById(usuarioDTO.getServiciosIds());
        if (servicios.size() != usuarioDTO.getServiciosIds().size()) {
            throw new ResourceNotFoundException(ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
        }

        // Validar que los horarios existan
        List<Horario> horarios = horarioRepository.findAllById(usuarioDTO.getHorariosIds());
        if (horarios.size() != usuarioDTO.getHorariosIds().size()) {
            throw new ResourceNotFoundException(ValidationErrorMessages.HORARIOS_NO_ENCONTRADOS);
        }
    }

    private void gestionarDatosTrabajador(Usuario usuario, UsuarioDTO usuarioDTO) {
        // Establecer el ID del usuario en el contrato
        usuarioDTO.getContrato().setUsuarioId(usuario.getId());
        contratoService.crear(usuarioDTO.getContrato(), usuarioDTO.getDocumentoContrato());
        
        List<Servicio> servicios = servicioRepository.findAllById(usuarioDTO.getServiciosIds());
        usuario.setServicios(servicios);
        
        List<Horario> horarios = horarioRepository.findAllById(usuarioDTO.getHorariosIds());
        usuario.setHorarios(horarios);
        
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void updateFotoUrl(Integer id, String fotoUrl) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
            
        usuario.setFoto(fotoUrl);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public String getFotoUrl(Integer id) {
        return usuarioRepository.findById(id)
            .map(Usuario::getFoto)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
    }

    @Transactional
    public void deleteFoto(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        if (usuario.getFoto() != null) {
            try {
                Path filePath = Paths.get(UPLOAD_DIR, usuario.getFoto());
                Files.deleteIfExists(filePath);
                usuario.setFoto(null);
                usuarioRepository.save(usuario);
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
            }
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
} 