package com.jve.Service;

import com.jve.DTO.UsuarioDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Repository.UsuarioRepository;
import com.jve.Converter.UsuarioConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;

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

    public void deleteUsuario(Integer id) {
        usuarioRepository.deleteById(id);
    }
} 