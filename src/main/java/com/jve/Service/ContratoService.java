package com.jve.Service;

import com.jve.Converter.ContratoConverter;
import com.jve.DTO.ContratoDTO;
import com.jve.Entity.*;
import com.jve.Repository.ContratoRepository;
import com.jve.Repository.UsuarioRepository;
import com.jve.Repository.EstadoRepository;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
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
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final ContratoConverter contratoConverter;
    private final UsuarioRepository usuarioRepository;
    private final EstadoRepository estadoRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTodos() {
        Map<String, Object> response = new HashMap<>();
        List<ContratoDTO> contratos = contratoRepository.findAll()
            .stream()
            .map(contratoConverter::toDTO)
            .collect(Collectors.toList());
        
        response.put("mensaje", ResponseMessages.LISTA_RECUPERADA);
        response.put("contratos", contratos);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPorId(Integer id) {
        Map<String, Object> response = new HashMap<>();
        Contrato contrato = contratoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CONTRATO_NO_ENCONTRADO));
        
        verificarYActualizarEstadoContrato(contrato);
        
        response.put("mensaje", ResponseMessages.ENTIDAD_RECUPERADA);
        response.put("contrato", contratoConverter.toDTO(contrato));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPorUsuarioId(Integer usuarioId) {
        Map<String, Object> response = new HashMap<>();
        List<Contrato> contratos = contratoRepository.findByUsuarioId(usuarioId);
        
        // Verificar y actualizar estado de contratos temporales vencidos
        contratos.forEach(this::verificarYActualizarEstadoContrato);
        
        List<ContratoDTO> contratosDTO = contratos.stream()
            .map(contratoConverter::toDTO)
            .collect(Collectors.toList());
        
        response.put("mensaje", ValidationErrorMessages.CONTRATOS_USUARIO_RECUPERADOS);
        response.put("contratos", contratosDTO);
        return response;
    }

    @Transactional
    public Map<String, Object> crear(ContratoDTO contratoDTO, MultipartFile documento) {
        Map<String, Object> response = new HashMap<>();

        Usuario usuario = usuarioRepository.findById(contratoDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        if (usuario.getRol() != RolUsuario.trabajador) {
            throw new RuntimeException(ValidationErrorMessages.USUARIO_NO_ES_TRABAJADOR);
        }

        // Validar que no tenga un contrato activo o pendiente
        if (contratoRepository.existsByUsuarioIdAndEstadoNombreIn(
                usuario.getId(), 
                Arrays.asList("ACTIVO", "PENDIENTE"))) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_YA_EXISTE);
        }

        validarFechasContrato(contratoDTO);

        try {
            // Crear directorio si no existe
            Path uploadPath = Paths.get("uploads", "contratos");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para el archivo
            String fileName = String.format("contrato_%d_%d.pdf", 
                usuario.getId(), System.currentTimeMillis());
            
            // Guardar el archivo
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(documento.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Establecer la URL del contrato
            contratoDTO.setUrlContrato("/contratos/" + fileName);

            // Ignoramos el estado que venga del frontend y lo asignamos según la fecha
            contratoDTO.setEstadoId(null);
            String estadoNombre = contratoDTO.getFechaInicioContrato().compareTo(new Date()) <= 0 ? "ACTIVO" : "PENDIENTE";
            Estado estado = estadoRepository.findByNombreAndTipoEstado(estadoNombre, TipoEstado.CONTRATO)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));
            contratoDTO.setEstadoId(estado.getId());

            Contrato contrato = contratoConverter.toEntity(contratoDTO);
            Contrato contratoGuardado = contratoRepository.save(contrato);

            response.put("mensaje", ResponseMessages.CONTRATO_CREADO);
            response.put("contrato", contratoConverter.toDTO(contratoGuardado));
            return response;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el documento del contrato: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, ContratoDTO contratoDTO) {
        Map<String, Object> response = new HashMap<>();

        Contrato contratoExistente = contratoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CONTRATO_NO_ENCONTRADO));

        // Mantener los datos existentes que no se van a actualizar
        if (contratoDTO.getEstadoId() == null) {
            contratoDTO.setEstadoId(contratoExistente.getEstado().getId());
        }
        if (contratoDTO.getFechaFinContrato() == null) {
            contratoDTO.setFechaFinContrato(contratoExistente.getFechaFinContrato());
        }
        if (contratoDTO.getUrlContrato() == null) {
            contratoDTO.setUrlContrato(contratoExistente.getUrlContrato());
        }

        // Campos que no se pueden modificar
        contratoDTO.setId(id);
        contratoDTO.setUsuarioId(contratoExistente.getUsuario().getId());
        contratoDTO.setTipoContrato(contratoExistente.getTipoContrato());
        contratoDTO.setFechaInicioContrato(contratoExistente.getFechaInicioContrato());

        // Si es contrato fijo y se cambia a INACTIVO → establecer fecha fin actual
        if (contratoExistente.getTipoContrato() == TipoContrato.fijo &&
            contratoDTO.getEstadoId() != null &&
            !contratoDTO.getEstadoId().equals(contratoExistente.getEstado().getId()) &&
            estadoRepository.findById(contratoDTO.getEstadoId())
                .map(estado -> estado.getNombre().equalsIgnoreCase("INACTIVO"))
                .orElse(false)) {

            contratoDTO.setFechaFinContrato(new Date());
        }

        // Validar fechas según tipo de contrato
        validarFechasContrato(contratoDTO);

        // Convertir y guardar
        Contrato actualizado = contratoConverter.toEntity(contratoDTO);
        contratoRepository.save(actualizado);

        response.put("mensaje", ResponseMessages.CONTRATO_ACTUALIZADO);
        response.put("contrato", contratoConverter.toDTO(actualizado));
        return response;
    }

    private void validarFechasContrato(ContratoDTO contratoDTO) {
        if (contratoDTO.getTipoContrato() == TipoContrato.temporal && contratoDTO.getFechaFinContrato() == null) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_TEMPORAL_REQUIERE_FECHA_FIN);
        }
        
        if (contratoDTO.getTipoContrato() == TipoContrato.fijo && contratoDTO.getFechaFinContrato() != null) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_FIJO_NO_FECHA_FIN);
        }

        if (contratoDTO.getFechaFinContrato() != null && 
            contratoDTO.getFechaFinContrato().before(contratoDTO.getFechaInicioContrato())) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_FECHA_FIN_ANTERIOR_INICIO);
        }
    }

    @Transactional
    private void verificarYActualizarEstadoContrato(Contrato contrato) {
        if (contrato.getTipoContrato() == TipoContrato.temporal &&
            contrato.getFechaFinContrato() != null &&
            contrato.getFechaFinContrato().before(new Date()) &&
            contrato.getEstado().getNombre().equals("ACTIVO")) {
            
            // Buscar estado INACTIVO
            contrato.setEstado(estadoRepository.findByNombreAndTipoEstado("INACTIVO", TipoEstado.CONTRATO)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO)));
            
            contratoRepository.save(contrato);
        }
    }

    //@Scheduled(cron = "0 0 0 * * ?") // Todos los días a medianoche
    @Scheduled(cron = "0 */1 * * * ?") // Cada minuto
    @Transactional
    public void actualizarEstadosContratosProgramado() {
        Date hoy = new Date();

        // Activar contratos que empiezan hoy (PENDIENTES → ACTIVOS)
        List<Contrato> contratosPendientes = contratoRepository
            .findByEstadoNombreAndFechaInicioContratoLessThanEqual("PENDIENTE", hoy);

        Estado estadoActivo = estadoRepository.findByNombreAndTipoEstado("ACTIVO", TipoEstado.CONTRATO)
            .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));

        for (Contrato contrato : contratosPendientes) {
            contrato.setEstado(estadoActivo);
            contratoRepository.save(contrato);
        }

        // Finalizar contratos temporales cuya fecha fin ya llegó (ACTIVOS → INACTIVOS)
        List<Contrato> contratosFinalizados = contratoRepository
            .findByEstadoNombreAndTipoContratoAndFechaFinContratoLessThanEqual(
                "ACTIVO", TipoContrato.temporal, hoy
            );

        Estado estadoInactivo = estadoRepository.findByNombreAndTipoEstado("INACTIVO", TipoEstado.CONTRATO)
            .orElseThrow(() -> new RuntimeException("Estado INACTIVO no encontrado"));

        for (Contrato contrato : contratosFinalizados) {
            contrato.setEstado(estadoInactivo);
            contratoRepository.save(contrato);
        }

        System.out.println("Tarea programada ejecutada: estados de contratos actualizados.");
    }

} 