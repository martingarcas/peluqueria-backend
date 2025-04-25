package com.jve.Controller;

import com.jve.DTO.LoginRequestDTO;
import com.jve.DTO.RegistroRequestDTO;
import com.jve.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registro(
            @Valid @RequestBody RegistroRequestDTO request,
            BindingResult result) {
        
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage()
                ));
            response.put("mensaje", "Error de validación");
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            return ResponseEntity.ok(authService.registro(request));
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDTO request,
            BindingResult result) {
        
        if (result.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            Map<String, String> errores = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    error -> error.getField(),
                    error -> error.getDefaultMessage()
                ));
            response.put("mensaje", "Error de validación");
            response.put("errores", errores);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (BadCredentialsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Credenciales incorrectas");
            return ResponseEntity.status(401).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 