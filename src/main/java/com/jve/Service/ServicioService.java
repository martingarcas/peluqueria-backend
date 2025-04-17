package com.jve.Service;

import com.jve.Converter.ServicioConverter;
import com.jve.DTO.ServicioDTO;
import com.jve.Entity.Servicio;
import com.jve.Entity.Usuario;
import com.jve.Entity.RolUsuario;
import com.jve.Repository.ServicioRepository;
import com.jve.Repository.UsuarioRepository;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicioConverter converter;

    private void validarDatosServicio(String nombre, String descripcion, Integer duracion, BigDecimal precio) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_NOMBRE_REQUERIDO);
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_DESCRIPCION_REQUERIDA);
        }
        if (duracion == null) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_DURACION_REQUERIDA);
        }
        if (duracion <= 0) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_DURACION_POSITIVA);
        }
        if (precio == null) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_PRECIO_REQUERIDO);
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_PRECIO_POSITIVO);
        }
    }

    public Map<String, Object> crear(ServicioDTO servicioDTO) {
        validarDatosServicio(
            servicioDTO.getNombre(),
            servicioDTO.getDescripcion(),
            servicioDTO.getDuracion(),
            servicioDTO.getPrecio()
        );

        if (servicioRepository.existsByNombre(servicioDTO.getNombre())) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_NOMBRE_DUPLICADO);
        }

        Servicio servicio = converter.toEntity(servicioDTO);
        servicio = servicioRepository.save(servicio);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.SERVICIO_CREADO);
        response.put("servicio", converter.toDTO(servicio));
        return response;
    }

    public Map<String, Object> obtenerPorId(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format(ValidationErrorMessages.SERVICIO_NO_ENCONTRADO, id)));

        Map<String, Object> response = new HashMap<>();
        response.put("servicio", converter.toDTO(servicio));
        return response;
    }

    public Map<String, Object> listarTodos() {
        List<ServicioDTO> serviciosDTO = servicioRepository.findAll().stream()
                .map(converter::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.SERVICIOS_LISTADOS);
        response.put("servicios", serviciosDTO);
        return response;
    }

    public Map<String, Object> listarPorTrabajador(Integer trabajadorId) {
        Usuario trabajador = usuarioRepository.findById(trabajadorId)
                .orElseThrow(() -> new EntityNotFoundException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        if (trabajador.getRol() != RolUsuario.trabajador) {
            throw new IllegalArgumentException(ValidationErrorMessages.USUARIO_NO_ES_TRABAJADOR);
        }

        List<ServicioDTO> serviciosDTO = servicioRepository.findByUsuariosId(trabajadorId).stream()
                .map(converter::toDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("servicios", serviciosDTO);
        return response;
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, ServicioDTO servicioDTO) {
        Servicio existente = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format(ValidationErrorMessages.SERVICIO_NO_ENCONTRADO, id)));

        // Si todos los datos son idénticos, no hacemos update
        if (existente.getNombre().equals(servicioDTO.getNombre()) &&
            existente.getDescripcion().equals(servicioDTO.getDescripcion()) &&
            existente.getDuracion().equals(servicioDTO.getDuracion()) &&
            existente.getPrecio().equals(servicioDTO.getPrecio())) {
            throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
        }

        validarDatosServicio(
            servicioDTO.getNombre(),
            servicioDTO.getDescripcion(),
            servicioDTO.getDuracion(),
            servicioDTO.getPrecio()
        );

        // Si el nombre es diferente, validar que no exista
        if (!existente.getNombre().equals(servicioDTO.getNombre()) &&
            servicioRepository.existsByNombre(servicioDTO.getNombre())) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIO_NOMBRE_DUPLICADO);
        }

        existente.setNombre(servicioDTO.getNombre());
        existente.setDescripcion(servicioDTO.getDescripcion());
        existente.setDuracion(servicioDTO.getDuracion());
        existente.setPrecio(servicioDTO.getPrecio());

        Servicio actualizado = servicioRepository.save(existente);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.SERVICIO_ACTUALIZADO);
        response.put("servicio", converter.toDTO(actualizado));
        return response;
    }

    public Map<String, Object> eliminar(Integer id) {
        if (!servicioRepository.existsById(id)) {
            throw new EntityNotFoundException(
                String.format(ValidationErrorMessages.SERVICIO_NO_ENCONTRADO, id));
        }
        servicioRepository.deleteById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ResponseMessages.SERVICIO_ELIMINADO);
        return response;
    }

    @Transactional
    public Map<String, Object> asignarServiciosATrabajador(Integer trabajadorId, List<Integer> serviciosIds) {
        Usuario trabajador = usuarioRepository.findById(trabajadorId)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        if (trabajador.getRol() != RolUsuario.trabajador) {
            throw new RuntimeException(ValidationErrorMessages.USUARIO_NO_ES_TRABAJADOR);
        }

        List<Servicio> servicios = servicioRepository.findAllById(serviciosIds);
        if (servicios.size() != serviciosIds.size()) {
            throw new RuntimeException(ValidationErrorMessages.SERVICIOS_NO_ENCONTRADOS);
        }

        trabajador.setServicios(servicios);
        usuarioRepository.save(trabajador);

        return Map.of("mensaje", ResponseMessages.SERVICIOS_ASIGNADOS);
    }
} 