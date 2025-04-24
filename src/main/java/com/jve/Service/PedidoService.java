package com.jve.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jve.DTO.PedidoDTO;
import com.jve.Entity.*;
import com.jve.Exception.ValidationErrorMessages;
import com.jve.Repository.*;
import com.jve.Converter.PedidoConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);
    private final PedidoRepository pedidoRepository;
    private final PedidoProductoRepository pedidoProductoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final EstadoRepository estadoRepository;
    private final PedidoConverter pedidoConverter;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> crearPedido(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));

        if (usuario.getCarrito() == null || usuario.getCarrito().equals("[]")) {
            throw new RuntimeException("El carrito está vacío");
        }

        try {
            // Convertir el carrito de JSON a List<CarritoRequest>
            List<PedidoDTO.CarritoRequest> carritoItems = objectMapper.readValue(usuario.getCarrito(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, PedidoDTO.CarritoRequest.class));

            // Obtener estado inicial (ACEPTADO)
            logger.info("Buscando estado ACEPTADO para tipo PEDIDO");
            Estado estadoInicial = estadoRepository.findById(2)  // Usar findById directamente con Integer
                .orElseThrow(() -> new RuntimeException("Estado ACEPTADO no encontrado para pedidos"));
            logger.info("Estado encontrado: id={}, nombre={}, tipo={}", 
                estadoInicial.getId(), 
                estadoInicial.getNombre(), 
                estadoInicial.getTipoEstado());

            // Asegurarnos de que el usuario está actualizado desde la base de datos
            usuario = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Calcular total
            BigDecimal total = BigDecimal.ZERO;
            for (PedidoDTO.CarritoRequest item : carritoItems) {
                Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));
                total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
            }

            // Crear el pedido usando setters
            logger.info("Creando pedido con Usuario ID: {}, Estado ID: {}, Total: {}", 
                usuario.getId(), estadoInicial.getId(), total);
            
            Pedido pedido = new Pedido();
            pedido.setFechaPedido(LocalDateTime.now());
            pedido.setUsuario(usuario);
            pedido.setEstado(estadoInicial);
            pedido.setTotal(total);
            pedido.setPedidoProductos(new ArrayList<>());
            
            // Primero guardamos el pedido para tener su ID
            pedido = pedidoRepository.save(pedido);
            logger.info("Pedido guardado inicialmente con ID: {}", pedido.getId());

            // Crear y guardar líneas de pedido
            List<PedidoProducto> lineasPedido = new ArrayList<>();
            for (PedidoDTO.CarritoRequest item : carritoItems) {
                Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

                // Validar stock
                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                }

                // Actualizar stock
                producto.setStock(producto.getStock() - item.getCantidad());
                productoRepository.save(producto);

                // Crear línea de pedido
                PedidoProducto lineaPedido = new PedidoProducto();
                lineaPedido.setPedido(pedido);  // Ahora el pedido ya tiene ID
                lineaPedido.setProducto(producto);
                lineaPedido.setCantidad(item.getCantidad());
                lineaPedido.setPrecioUnitario(producto.getPrecio());
                lineaPedido.setNombreProducto(producto.getNombre());
                lineasPedido.add(lineaPedido);
            }

            // Guardar las líneas de pedido
            pedidoProductoRepository.saveAll(lineasPedido);
            
            // Actualizar el pedido con las líneas y guardarlo de nuevo
            pedido.setPedidoProductos(lineasPedido);
            pedido = pedidoRepository.save(pedido);

            // Vaciar carrito
            usuario.setCarrito("[]");
            usuarioRepository.save(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Pedido creado exitosamente");
            response.put("pedido", pedidoConverter.toDto(pedido));
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error al crear el pedido: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPedidosUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException(ValidationErrorMessages.USUARIO_NO_ENCONTRADO));
        
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(usuario.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Pedidos recuperados exitosamente");
        response.put("pedidos", pedidoConverter.toDtoList(pedidos));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerTodosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Pedidos recuperados exitosamente");
        response.put("pedidos", pedidoConverter.toDtoList(pedidos));
        return response;
    }

    @Transactional
    public Map<String, Object> actualizarEstadoPedido(Integer pedidoId, String nuevoEstadoNombre) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validar si el pedido puede ser modificado
        if (pedido.getEstado().getNombre().equalsIgnoreCase("COMPLETADO") || 
            pedido.getEstado().getNombre().equalsIgnoreCase("CANCELADO")) {
            throw new RuntimeException("No se puede modificar el estado de un pedido completado o cancelado");
        }

        // Buscar el nuevo estado
        Estado nuevoEstado = estadoRepository.findByNombreAndTipoEstado(nuevoEstadoNombre.toUpperCase(), TipoEstado.PEDIDO)
            .orElseThrow(() -> new RuntimeException("Estado no válido para pedidos"));

        pedido.setEstado(nuevoEstado);
        pedidoRepository.save(pedido);

        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Estado del pedido actualizado exitosamente");
        response.put("pedido", pedidoConverter.toDto(pedido));
        return response;
    }
} 