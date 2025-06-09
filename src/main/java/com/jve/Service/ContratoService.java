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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.math.BigDecimal;
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
    public Map<String, Object> crear(Integer usuarioId, String fechaInicioContrato, 
            String fechaFinContrato, String tipoContrato, MultipartFile documento, BigDecimal salario) {
        
        System.out.println("=== DEBUG: Datos recibidos en crear contrato ===");
        System.out.println("usuarioId: " + usuarioId);
        System.out.println("fechaInicioContrato: " + fechaInicioContrato);
        System.out.println("fechaFinContrato: " + fechaFinContrato);
        System.out.println("tipoContrato: " + tipoContrato);
        System.out.println("documento: " + (documento != null ? "Presente" : "Null"));
        System.out.println("salario: " + salario);
        
        if (fechaInicioContrato == null || fechaInicioContrato.trim().isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_FECHA_INICIO_REQUERIDA);
        }
        
        ContratoDTO contratoDTO = new ContratoDTO();
        contratoDTO.setUsuarioId(usuarioId);
        contratoDTO.setFechaInicioContrato(java.sql.Date.valueOf(fechaInicioContrato));
        if (fechaFinContrato != null && !fechaFinContrato.isEmpty()) {
            contratoDTO.setFechaFinContrato(java.sql.Date.valueOf(fechaFinContrato));
        }
        contratoDTO.setTipoContrato(TipoContrato.valueOf(tipoContrato.toLowerCase()));
        contratoDTO.setSalario(salario);

        return crear(contratoDTO, documento);
    }

    @Transactional
    public Map<String, Object> crear(ContratoDTO contratoDTO, MultipartFile documento) {
        Map<String, Object> response = new HashMap<>();

        // 1. Validar y obtener usuario
        Usuario usuario = usuarioRepository.findById(contratoDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        if (usuario.getRol() != RolUsuario.trabajador) {
            throw new RuntimeException(ValidationErrorMessages.USUARIO_NO_ES_TRABAJADOR);
        }

        // 2. Verificar contratos existentes
        List<Contrato> contratosExistentes = contratoRepository.findByUsuarioId(usuario.getId());
        boolean tieneContratoActivoOPendiente = contratosExistentes.stream()
            .anyMatch(c -> c.getEstado().getNombre().equals("ACTIVO") || 
                         c.getEstado().getNombre().equals("PENDIENTE"));
        
        if (tieneContratoActivoOPendiente) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_YA_EXISTE);
        }

        // 3. Validar fechas
        validarFechasContrato(contratoDTO);

        try {
            // 4. Procesar documento si existe
            if (documento != null && !documento.isEmpty()) {
                Path uploadPath = Paths.get("uploads", "contratos");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = String.format("contrato_%d_%d.pdf", 
                    usuario.getId(), System.currentTimeMillis());
                
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(documento.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                contratoDTO.setUrlContrato("/contratos/" + fileName);
            }

            // 5. Determinar y obtener estado
            String estadoNombre = contratoDTO.getFechaInicioContrato().compareTo(new java.sql.Date(System.currentTimeMillis())) <= 0 ? "ACTIVO" : "PENDIENTE";
            Estado estado = estadoRepository.findByNombreAndTipoEstado(estadoNombre, TipoEstado.CONTRATO)
                .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));

            // 6. Crear y guardar contrato
            Contrato contrato = contratoConverter.toEntity(contratoDTO);
            contrato.setUsuario(usuario);
            contrato.setEstado(estado);
            
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

        // Si no hay cambios, retornar 304
        if ((contratoDTO.getEstadoId() == null || contratoDTO.getEstadoId().equals(contratoExistente.getEstado().getId())) &&
            (contratoDTO.getFechaFinContrato() == null || contratoDTO.getFechaFinContrato().equals(contratoExistente.getFechaFinContrato())) &&
            (contratoDTO.getUrlContrato() == null || contratoDTO.getUrlContrato().equals(contratoExistente.getUrlContrato()))) {
            throw new ResponseStatusException(HttpStatus.NOT_MODIFIED);
        }

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

    @Transactional(readOnly = true)
    public Contrato obtenerContratoActual(Integer usuarioId) {
        return contratoRepository.findByUsuarioIdAndEstadoNombre(usuarioId, "ACTIVO")
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CONTRATO_NO_ENCONTRADO));
    }

    @Transactional(readOnly = true)
    public Resource descargarPDF(Integer usuarioId) {
        // Obtener el contrato más reciente del usuario
        List<Contrato> contratos = contratoRepository.findByUsuarioId(usuarioId);
        if (contratos.isEmpty()) {
            throw new RuntimeException(ValidationErrorMessages.CONTRATO_NO_ENCONTRADO);
        }
        
        // Ordenar por fecha de inicio descendente y tomar el primero
        Contrato contrato = contratos.stream()
            .sorted((c1, c2) -> c2.getFechaInicioContrato().compareTo(c1.getFechaInicioContrato()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.CONTRATO_NO_ENCONTRADO));

        if (contrato.getUrlContrato() == null) {
            throw new RuntimeException(ValidationErrorMessages.ARCHIVO_NO_ENCONTRADO);
        }

        // Construir la ruta al archivo
        Path filePath = Paths.get("uploads").resolve(contrato.getUrlContrato().substring(1));
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            throw new RuntimeException(ValidationErrorMessages.ARCHIVO_NO_ENCONTRADO);
        }

        return resource;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verificarContratoActivo(Integer trabajadorId) {
        boolean tieneContratoActivo = contratoRepository.existsByUsuarioIdAndEstadoNombre(trabajadorId, "ACTIVO");
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Verificación de contrato completada");
        response.put("tieneContratoActivo", tieneContratoActivo);
        return response;
    }

} 