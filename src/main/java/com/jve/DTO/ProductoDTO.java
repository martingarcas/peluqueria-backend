package com.jve.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import com.jve.Exception.ValidationErrorMessages;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private Integer id;
    
    @NotBlank(message = ValidationErrorMessages.PRODUCTO_NOMBRE_REQUERIDO)
    private String nombre;
    
    @NotBlank(message = ValidationErrorMessages.PRODUCTO_DESCRIPCION_REQUERIDA)
    private String descripcion;
    
    @NotNull(message = ValidationErrorMessages.PRODUCTO_PRECIO_REQUERIDO)
    @Positive(message = ValidationErrorMessages.PRODUCTO_PRECIO_POSITIVO)
    private BigDecimal precio;
    
    @NotNull(message = ValidationErrorMessages.PRODUCTO_STOCK_REQUERIDO)
    @Min(value = 0, message = ValidationErrorMessages.PRODUCTO_STOCK_MINIMO)
    private Integer stock;
    
    private Integer categoriaId;
    private String categoriaNombre;

    // Método para crear un nuevo producto (sin id ni categoría)
    public static ProductoDTO crearNuevo(String nombre, String descripcion, BigDecimal precio, Integer stock) {
        ProductoDTO dto = new ProductoDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        dto.setPrecio(precio);
        dto.setStock(stock);
        return dto;
    }

    // Método para actualizar un producto existente
    public void actualizarDatos(String nombre, String descripcion, BigDecimal precio, Integer stock) {
        if (nombre != null) this.nombre = nombre;
        if (descripcion != null) this.descripcion = descripcion;
        if (precio != null) this.precio = precio;
        if (stock != null) this.stock = stock;
    }

    // Método para validar si hay cambios en una actualización
    public boolean tieneModificaciones(ProductoDTO otro) {
        if (otro == null) return true;
        return !this.nombre.equals(otro.nombre) ||
               !this.descripcion.equals(otro.descripcion) ||
               !this.precio.equals(otro.precio) ||
               !this.stock.equals(otro.stock);
    }

    // Método para crear una copia para actualización
    public ProductoDTO copiaParaActualizar() {
        ProductoDTO copia = new ProductoDTO();
        copia.setId(this.id);
        copia.setNombre(this.nombre);
        copia.setDescripcion(this.descripcion);
        copia.setPrecio(this.precio);
        copia.setStock(this.stock);
        copia.setCategoriaId(this.categoriaId);
        return copia;
    }
} 