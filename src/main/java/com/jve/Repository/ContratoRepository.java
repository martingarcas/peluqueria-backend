package com.jve.Repository;

import com.jve.Entity.Contrato;
import com.jve.Entity.TipoContrato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Integer> {
    List<Contrato> findByUsuarioId(Integer usuarioId);
    Optional<Contrato> findByUsuarioIdAndEstadoNombre(Integer usuarioId, String estadoNombre);
    boolean existsByUsuarioId(Integer usuarioId);
    boolean existsByUsuarioIdAndEstadoNombre(Integer usuarioId, String estadoNombre);
    boolean existsByUsuarioIdAndEstadoNombreIn(Integer usuarioId, List<String> estados);

    // Para activar contratos pendientes cuyo inicio ya llegó
    List<Contrato> findByEstadoNombreAndFechaInicioContratoLessThanEqual(String estado, Date fecha);
    // Para finalizar contratos temporales cuyo fin ya llegó
    List<Contrato> findByEstadoNombreAndTipoContratoAndFechaFinContratoLessThanEqual(
        String estado,
        TipoContrato tipoContrato,
        Date fecha
    );

} 