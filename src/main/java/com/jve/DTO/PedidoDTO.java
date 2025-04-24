package com.jve.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoDTO {
    private Integer id;
    private LocalDateTime fechaPedido;
    private String estado;
    private BigDecimal total;
    private RegistroResponseDTO usuario;
    private List<LineaPedidoDTO> lineasPedido;

    @Data
    public static class LineaPedidoDTO {
        private Integer id;
        private Integer productoId;
        private String nombreProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }

    @Data
    public static class CarritoRequest {
        @NotNull(message = "El ID del producto es obligatorio")
        private Integer productoId;
        
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor que 0")
        private Integer cantidad;
    }

    @Data
    public static class ActualizarEstadoRequest {
        @NotBlank(message = "El estado es obligatorio")
        private String estado;
    }
} 