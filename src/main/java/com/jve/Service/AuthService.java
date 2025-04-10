package com.jve.Service;

import com.jve.DTO.LoginRequestDTO;
import com.jve.DTO.LoginResponseDTO;
import com.jve.DTO.RegistroRequestDTO;
import com.jve.DTO.RegistroResponseDTO;
import com.jve.Entity.Usuario;
import com.jve.Entity.RolUsuario;
import com.jve.Repository.UsuarioRepository;
import com.jve.Security.JwtTokenProvider;
import com.jve.Converter.UsuarioConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public RegistroResponseDTO registro(RegistroRequestDTO request) {
        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setApellidos(request.apellidos());
        usuario.setDireccion(request.direccion());
        usuario.setTelefono(request.telefono());
        usuario.setRol(request.rol() != null ? request.rol() : RolUsuario.cliente); // Usar el rol especificado o cliente por defecto

        // Guardar usuario
        Usuario savedUsuario = usuarioRepository.save(usuario);
        return usuarioConverter.toResponseDTO(savedUsuario);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        // Autenticar usuario usando email
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // Obtener usuario
        Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar token
        String token = jwtTokenProvider.generateToken(authentication);

        // Crear respuesta
        return new LoginResponseDTO(
            usuario.getEmail(), // Usamos email como username
            List.of(usuario.getRol().name()),
            token,
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol().name(),
            usuario.getDireccion(),
            usuario.getTelefono(),
            usuario.getFoto()
        );
    }
} 