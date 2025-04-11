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
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public Map<String, Object> registro(RegistroRequestDTO request) {
        // Verificar si el usuario ya existe
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_EMAIL_YA_REGISTRADO);
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setApellidos(request.apellidos());
        usuario.setDireccion(request.direccion());
        usuario.setTelefono(request.telefono());
        usuario.setRol(request.rol() != null ? request.rol() : RolUsuario.cliente);

        // Guardar usuario
        Usuario savedUsuario = usuarioRepository.save(usuario);
        RegistroResponseDTO responseDTO = usuarioConverter.toResponseDTO(savedUsuario);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.AUTH_REGISTRO_EXITOSO);
        response.put("usuario", responseDTO);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(LoginRequestDTO request) {
        try {
            // Autenticar usuario usando email
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // Obtener usuario
            Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.AUTH_USUARIO_NO_ENCONTRADO));

            // Generar token
            String token = jwtTokenProvider.generateToken(authentication);

            // Crear respuesta
            LoginResponseDTO responseDTO = new LoginResponseDTO(
                usuario.getEmail(),
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", ResponseMessages.AUTH_LOGIN_EXITOSO);
            response.put("data", responseDTO);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(ValidationErrorMessages.AUTH_CREDENCIALES_INCORRECTAS);
        }
    }
} 