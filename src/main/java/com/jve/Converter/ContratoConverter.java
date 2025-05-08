package com.jve.Converter;

import com.jve.DTO.ContratoDTO;
import com.jve.Entity.Contrato;
import com.jve.Entity.Estado;
import com.jve.Entity.TipoEstado;
import com.jve.Entity.Usuario;
import com.jve.Repository.EstadoRepository;
import com.jve.Repository.UsuarioRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContratoConverter {
    
    private final UsuarioRepository usuarioRepository;
    private final EstadoRepository estadoRepository;

    public ContratoDTO toDTO(Contrato contrato) {
        if (contrato == null) return null;
        
        ContratoDTO dto = new ContratoDTO();
        dto.setId(contrato.getId());
        dto.setUsuarioId(contrato.getUsuario().getId());
        dto.setNombreUsuario(contrato.getUsuario().getNombre());
        dto.setFechaInicioContrato(contrato.getFechaInicioContrato());
        dto.setFechaFinContrato(contrato.getFechaFinContrato());
        dto.setTipoContrato(contrato.getTipoContrato());
        dto.setEstadoId(contrato.getEstado().getId());
        dto.setEstadoNombre(contrato.getEstado().getNombre());
        dto.setUrlContrato(contrato.getUrlContrato());
        dto.setSalario(contrato.getSalario());
        
        return dto;
    }

    public Contrato toEntity(ContratoDTO dto) {
        if (dto == null) return null;
    
        Contrato contrato = new Contrato();
        if (dto.getId() != null) {
            contrato.setId(dto.getId());
        }
    
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
        Estado estado = estadoRepository.findById(dto.getEstadoId())
            .filter(e -> e.getTipoEstado() == TipoEstado.CONTRATO)
            .orElseThrow(() -> new RuntimeException("El estado indicado no es válido para contratos"));
    
        contrato.setUsuario(usuario);
        contrato.setFechaInicioContrato(dto.getFechaInicioContrato());
        contrato.setFechaFinContrato(dto.getFechaFinContrato());
        contrato.setTipoContrato(dto.getTipoContrato());
        contrato.setEstado(estado);
        contrato.setUrlContrato(dto.getUrlContrato());
        contrato.setSalario(dto.getSalario());
    
        return contrato;
    }
    
} 