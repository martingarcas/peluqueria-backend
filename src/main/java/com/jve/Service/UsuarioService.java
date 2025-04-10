package com.jve.Service;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Repository.UsuarioRepository;
import com.jve.Converter.UsuarioConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final String UPLOAD_DIR = "uploads/users/";

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public UsuarioDTO createUsuario(UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioConverter.toEntity(usuarioDTO);
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return usuarioConverter.toDTO(savedUsuario);
    }

    public Optional<RegistroResponseDTO> updateUsuario(Integer id, UsuarioDTO usuarioDTO) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setApellidos(usuarioDTO.getApellidos());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setDireccion(usuarioDTO.getDireccion());
            usuario.setTelefono(usuarioDTO.getTelefono());

            if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
            }

            usuarioRepository.save(usuario);
            return usuarioConverter.toResponseDTO(usuario);
        });
    }

    public boolean deleteUsuario(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void updateFotoUrl(Integer id, String fotoUrl) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setFoto(fotoUrl);
            usuarioRepository.save(usuario);
        });
    }

    public String getFotoUrl(Integer id) {
        return usuarioRepository.findById(id)
                .map(Usuario::getFoto)
                .orElse(null);
    }

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