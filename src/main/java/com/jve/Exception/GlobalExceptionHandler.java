package com.jve.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String mensaje = "Error en el formato de los datos";
        
        // Detectar errores específicos de tipo
        String errorMessage = e.getMessage().toLowerCase();
        if (errorMessage.contains("integer") || errorMessage.contains("number")) {
            mensaje = "El campo debe ser un número válido";
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("error", mensaje);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "No tienes permisos para realizar esta operación");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
} 