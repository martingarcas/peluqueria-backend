package com.jve.Service;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Repository.UsuarioRepository;
import com.jve.Converter.UsuarioConverter;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final String UPLOAD_DIR = "uploads/users/";

    @Transactional(readOnly = true)
    public Map<String, Object> getAllUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIOS_LISTADOS);
        response.put("usuarios", usuarios.stream()
            .map(usuarioConverter::toResponseDTO)
            .collect(java.util.stream.Collectors.toList()));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsuarioById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.AUTH_USUARIO_NO_ENCONTRADO));
        
        Map<String, Object> response = new HashMap<>();
        response.put("usuario", usuarioConverter.toResponseDTO(usuario));
        return response;
    }

    @Transactional
    public Map<String, Object> createUsuario(UsuarioDTO usuarioDTO) {
        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
        }

        Usuario usuario = usuarioConverter.toEntity(usuarioDTO);
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        Usuario savedUsuario = usuarioRepository.save(usuario);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_CREADO);
        response.put("usuario", usuarioConverter.toResponseDTO(savedUsuario));
        return response;
    }

    @Transactional
    public Map<String, Object> updateUsuario(Integer id, UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.AUTH_USUARIO_NO_ENCONTRADO));

        // Validar que el email no exista si se está cambiando
        if (!usuario.getEmail().equals(usuarioDTO.getEmail()) && 
            usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
        }

        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setApellidos(usuarioDTO.getApellidos());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setTelefono(usuarioDTO.getTelefono());

        if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        }

        Usuario updatedUsuario = usuarioRepository.save(usuario);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_ACTUALIZADO);
        response.put("usuario", usuarioConverter.toResponseDTO(updatedUsuario));
        return response;
    }

    @Transactional
    public Map<String, Object> deleteUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_USUARIO_NO_ENCONTRADO);
        }
        
        usuarioRepository.deleteById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.USUARIO_ELIMINADO);
        return response;
    }

    @Transactional
    public void updateFotoUrl(Integer id, String fotoUrl) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.AUTH_USUARIO_NO_ENCONTRADO));
            
        usuario.setFoto(fotoUrl);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public String getFotoUrl(Integer id) {
        return usuarioRepository.findById(id)
            .map(Usuario::getFoto)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.AUTH_USUARIO_NO_ENCONTRADO));
    }

    @Transactional
    public void deleteFoto(Integer id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            String oldFoto = usuario.getFoto();
            if (oldFoto != null && !oldFoto.isEmpty()) {
                try {
                    // Extraer solo el nombre del archivo de la URL
                    String fileName = oldFoto.substring(oldFoto.lastIndexOf('/') + 1);
                    Path filePath = Paths.get(UPLOAD_DIR, fileName);
                    
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        System.out.println("Archivo eliminado: " + filePath);
                    } else {
                        System.out.println("El archivo no existe: " + filePath);
                    }
                } catch (IOException e) {
                    System.err.println("Error al eliminar el archivo: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            usuario.setFoto(null);
            usuarioRepository.save(usuario);
        });
    }
} 