package com.jve.Service;

import com.jve.Converter.HorarioConverter;
import com.jve.DTO.HorarioDTO;
import com.jve.Entity.Horario;
import com.jve.Entity.RolUsuario;
import com.jve.Entity.Usuario;
import com.jve.Repository.HorarioRepository;
import com.jve.Repository.UsuarioRepository;
import com.jve.Exception.ResponseMessages;
import com.jve.Exception.ValidationErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioConverter horarioConverter;

    @Transactional
    public Map<String, Object> crear(HorarioDTO horarioDTO) {
        Map<String, Object> response = new HashMap<>();

        // Validar trabajadores
        if (horarioDTO.getTrabajadorIds() != null && !horarioDTO.getTrabajadorIds().isEmpty()) {
            for (Integer trabajadorId : horarioDTO.getTrabajadorIds()) {
                Usuario trabajador = usuarioRepository.findById(trabajadorId)
                    .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

                log.info("Validando rol del trabajador {}: {}", trabajadorId, trabajador.getRol());
                
                if (trabajador.getRol() != RolUsuario.trabajador) {
                    log.error("El usuario {} no tiene el rol correcto. Rol actual: {}", trabajadorId, trabajador.getRol());
                    throw new RuntimeException(ValidationErrorMessages.USUARIO_NO_ES_TRABAJADOR);
                }

                // Validar solapamiento para cada trabajador
                if (horarioRepository.existsByTrabajadoresAndDiaSemanaAndHoraInicioLessThanAndHoraFinGreaterThan(
                        trabajadorId,
                        horarioDTO.getDiaSemana(),
                        horarioDTO.getHoraFin(),
                        horarioDTO.getHoraInicio())) {
                    throw new RuntimeException(ValidationErrorMessages.HORARIO_SOLAPAMIENTO);
                }
            }
        }

        // Validar que hora fin sea después de hora inicio
        if (horarioDTO.getHoraFin().before(horarioDTO.getHoraInicio())) {
            throw new RuntimeException(ValidationErrorMessages.HORARIO_HORA_FIN_ANTERIOR);
        }

        Horario horario = horarioConverter.toEntity(horarioDTO);
        horario = horarioRepository.save(horario);

        response.put("mensaje", ResponseMessages.HORARIO_CREADO);
        response.put("horario", horarioConverter.toDTO(horario));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPorId(Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        Horario horario = horarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.HORARIO_NO_ENCONTRADO));

        response.put("horario", horarioConverter.toDTO(horario));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listarTodos() {
        Map<String, Object> response = new HashMap<>();
        
        List<Horario> horarios = horarioRepository.findAll();
        List<HorarioDTO> horariosDTO = horarios.stream()
            .map(horarioConverter::toDTO)
            .toList();

        response.put("horarios", horariosDTO);
        return response;
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, HorarioDTO horarioDTO) {
        Map<String, Object> response = new HashMap<>();

        Horario horarioExistente = horarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.HORARIO_NO_ENCONTRADO));

        // Validar trabajadores
        if (horarioDTO.getTrabajadorIds() != null && !horarioDTO.getTrabajadorIds().isEmpty()) {
            for (Integer trabajadorId : horarioDTO.getTrabajadorIds()) {
                Usuario trabajador = usuarioRepository.findById(trabajadorId)
                    .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

                log.info("Validando rol del trabajador {}: {}", trabajadorId, trabajador.getRol());
                
                if (trabajador.getRol() != RolUsuario.trabajador) {
                    log.error("El usuario {} no tiene el rol correcto. Rol actual: {}", trabajadorId, trabajador.getRol());
                    throw new RuntimeException(ValidationErrorMessages.USUARIO_NO_ES_TRABAJADOR);
                }

                // Validar solapamiento para cada trabajador
                if (horarioRepository.existsByTrabajadoresAndDiaSemanaAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
                        trabajadorId,
                        horarioDTO.getDiaSemana(),
                        horarioDTO.getHoraFin(),
                        horarioDTO.getHoraInicio(),
                        id)) {
                    throw new RuntimeException(ValidationErrorMessages.HORARIO_SOLAPAMIENTO);
                }
            }
        }

        // Validar que hora fin sea después de hora inicio
        if (horarioDTO.getHoraFin().before(horarioDTO.getHoraInicio())) {
            throw new RuntimeException(ValidationErrorMessages.HORARIO_HORA_FIN_ANTERIOR);
        }

        horarioDTO.setId(id);
        Horario horarioActualizado = horarioConverter.toEntity(horarioDTO);
        horarioActualizado = horarioRepository.save(horarioActualizado);

        response.put("mensaje", ResponseMessages.HORARIO_ACTUALIZADO);
        response.put("horario", horarioConverter.toDTO(horarioActualizado));
        return response;
    }

    @Transactional
    public Map<String, Object> eliminar(Integer id) {
        Map<String, Object> response = new HashMap<>();

        if (!horarioRepository.existsById(id)) {
            throw new RuntimeException(ValidationErrorMessages.HORARIO_NO_ENCONTRADO);
        }

        horarioRepository.deleteById(id);
        response.put("mensaje", ResponseMessages.HORARIO_ELIMINADO);
        return response;
    }
} 