package com.jve.Converter;

import com.jve.DTO.PedidoDTO;
import com.jve.Entity.Pedido;
import com.jve.Entity.PedidoProducto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PedidoConverter {
    
    private final UsuarioConverter usuarioConverter;

    public PedidoDTO toDto(Pedido pedido) {
        if (pedido == null) return null;

        PedidoDTO dto = new PedidoDTO();
        dto.setId(pedido.getId());
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setEstado(pedido.getEstado().getNombre());
        dto.setTotal(pedido.getTotal());
        dto.setUsuario(usuarioConverter.toResponseDTO(pedido.getUsuario()));
        dto.setLineasPedido(toLineaPedidoDTO(pedido.getPedidoProductos()));
        
        return dto;
    }

    private List<PedidoDTO.LineaPedidoDTO> toLineaPedidoDTO(List<PedidoProducto> lineas) {
        if (lineas == null) return null;

        return lineas.stream().map(linea -> {
            PedidoDTO.LineaPedidoDTO dto = new PedidoDTO.LineaPedidoDTO();
            dto.setId(linea.getId());
            dto.setProductoId(linea.getProducto().getId());
            dto.setNombreProducto(linea.getNombreProducto());
            dto.setCantidad(linea.getCantidad());
            dto.setPrecioUnitario(linea.getPrecioUnitario());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<PedidoDTO> toDtoList(List<Pedido> pedidos) {
        if (pedidos == null) return null;
        return pedidos.stream().map(this::toDto).collect(Collectors.toList());
    }
} 