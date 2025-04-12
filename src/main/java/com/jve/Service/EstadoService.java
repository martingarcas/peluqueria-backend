package com.jve.Service;

import com.jve.Converter.EstadoConverter;
import com.jve.DTO.EstadoDTO;
import com.jve.Entity.Estado;
import com.jve.Entity.TipoEstado;
import com.jve.Repository.EstadoRepository;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Exception.ResponseMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadoService {

    private final EstadoRepository estadoRepository;
    private final EstadoConverter estadoConverter;

    public Map<String, Object> obtenerPorTipo(String tipo) {
        Map<String, Object> response = new HashMap<>();
        List<EstadoDTO> estados = estadoRepository.findByTipoEstado(TipoEstado.valueOf(tipo))
            .stream()
            .map(estadoConverter::toDTO)
            .collect(Collectors.toList());
        
        response.put("mensaje", ResponseMessages.LISTA_RECUPERADA);
        response.put("estados", estados);
        return response;
    }

    public Map<String, Object> obtenerPorId(Integer id) {
        Map<String, Object> response = new HashMap<>();
        Estado estado = estadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));
        
        response.put("mensaje", ResponseMessages.ENTIDAD_RECUPERADA);
        response.put("estado", estadoConverter.toDTO(estado));
        return response;
    }

    @Transactional
    public Map<String, Object> crear(EstadoDTO estadoDTO) {
        Map<String, Object> response = new HashMap<>();
        
        if (estadoRepository.findByNombreAndTipoEstado(estadoDTO.getNombre(), estadoDTO.getTipoEstado()).isPresent()) {
            throw new RuntimeException(ValidationErrorMessages.ESTADO_YA_EXISTE);
        }

        Estado estado = estadoConverter.toEntity(estadoDTO);
        Estado estadoGuardado = estadoRepository.save(estado);
        
        response.put("mensaje", ResponseMessages.ESTADO_CREADO);
        response.put("estado", estadoConverter.toDTO(estadoGuardado));
        return response;
    }

    @Transactional
    public Map<String, Object> actualizar(Integer id, EstadoDTO estadoDTO) {
        Map<String, Object> response = new HashMap<>();
        
        Estado estadoExistente = estadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));
        
        Estado estadoNuevo = estadoConverter.toEntity(estadoDTO);
        
        // Si los datos son iguales, no actualizamos
        if (estadoExistente.datosIguales(estadoNuevo)) {
            response.put("mensaje", ResponseMessages.NO_CAMBIOS_NECESARIOS);
            response.put("estado", estadoConverter.toDTO(estadoExistente));
            return response;
        }

        estadoRepository.findByNombreAndTipoEstado(estadoDTO.getNombre(), estadoDTO.getTipoEstado())
            .ifPresent(estado -> {
                if (!estado.getId().equals(id)) {
                    throw new RuntimeException(ValidationErrorMessages.ESTADO_YA_EXISTE);
                }
            });

        estadoExistente.setNombre(estadoDTO.getNombre());
        estadoExistente.setTipoEstado(estadoDTO.getTipoEstado());
        Estado estadoActualizado = estadoRepository.save(estadoExistente);
        
        response.put("mensaje", ResponseMessages.ESTADO_ACTUALIZADO);
        response.put("estado", estadoConverter.toDTO(estadoActualizado));
        return response;
    }

    @Transactional
    public Map<String, Object> eliminar(Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        Estado estado = estadoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.ESTADO_NO_ENCONTRADO));
            
        estadoRepository.delete(estado);
        
        response.put("mensaje", ResponseMessages.ESTADO_ELIMINADO);
        return response;
    }

    @PostConstruct
    @Transactional
    public void inicializarEstados() {
        // Estados para Pedidos
        crearEstadoSiNoExiste("PENDIENTE", "PEDIDO");
        crearEstadoSiNoExiste("ACEPTADO", "PEDIDO");
        crearEstadoSiNoExiste("ENVIADO", "PEDIDO");
        crearEstadoSiNoExiste("COMPLETADO", "PEDIDO");
        crearEstadoSiNoExiste("CANCELADO", "PEDIDO");

        // Estados para Citas
        crearEstadoSiNoExiste("PROGRAMADA", "CITA");
        crearEstadoSiNoExiste("COMPLETADA", "CITA");
        crearEstadoSiNoExiste("CANCELADA", "CITA");

        // Estados para Contratos
        crearEstadoSiNoExiste("ACTIVO", "CONTRATO");
        crearEstadoSiNoExiste("PENDIENTE", "CONTRATO");
        crearEstadoSiNoExiste("INACTIVO", "CONTRATO");
    }

    private void crearEstadoSiNoExiste(String nombre, String tipo) {
        if (estadoRepository.findByNombreAndTipoEstado(nombre, TipoEstado.valueOf(tipo)).isEmpty()) {
            Estado estado = new Estado(nombre, tipo);
            estadoRepository.save(estado);
        }
    }

    public Map<String, Object> obtenerTodos() {
        Map<String, Object> response = new HashMap<>();
        List<EstadoDTO> estados = estadoRepository.findAll()
            .stream()
            .map(estadoConverter::toDTO)
            .collect(Collectors.toList());
        
        response.put("mensaje", ResponseMessages.LISTA_RECUPERADA);
        response.put("estados", estados);
        return response;
    }
} 