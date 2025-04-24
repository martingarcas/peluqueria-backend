package com.jve.Repository;

import com.jve.Entity.Estado;
import com.jve.Entity.TipoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    List<Estado> findByTipoEstado(TipoEstado tipo);
    Optional<Estado> findByNombreAndTipoEstado(String nombre, TipoEstado tipo);
    boolean existsByNombreAndTipoEstado(String nombre, TipoEstado tipoEstado);
} 