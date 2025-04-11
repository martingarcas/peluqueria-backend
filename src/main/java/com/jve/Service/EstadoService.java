package com.jve.Service;

import com.jve.Entity.Estado;
import com.jve.Repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class EstadoService {

    private final EstadoRepository estadoRepository;

    public Map<String, List<Estado>> obtenerPorTipo(String tipo) {
        Map<String, List<Estado>> response = new HashMap<>();
        List<Estado> estados = estadoRepository.findByTipoEstado(tipo);
        response.put("estados", estados);
        return response;
    }

    @Transactional
    public void inicializarEstados() {
        // Estados para Pedidos
        crearEstadoSiNoExiste("PENDIENTE", "PEDIDO");
        crearEstadoSiNoExiste("COMPLETADO", "PEDIDO");
        crearEstadoSiNoExiste("CANCELADO", "PEDIDO");

        // Estados para Citas
        crearEstadoSiNoExiste("PROGRAMADA", "CITA");
        crearEstadoSiNoExiste("COMPLETADA", "CITA");
        crearEstadoSiNoExiste("CANCELADA", "CITA");

        // Estados para Contratos
        crearEstadoSiNoExiste("ACTIVO", "CONTRATO");
        crearEstadoSiNoExiste("FINALIZADO", "CONTRATO");
        crearEstadoSiNoExiste("CANCELADO", "CONTRATO");
    }

    private void crearEstadoSiNoExiste(String nombre, String tipo) {
        if (estadoRepository.findByNombreAndTipoEstado(nombre, tipo).isEmpty()) {
            Estado estado = new Estado(nombre, tipo);
            estadoRepository.save(estado);
        }
    }
} 