package com.jve.Repository;

import com.jve.Entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    List<Estado> findByTipoEstado(String tipoEstado);
    Optional<Estado> findByNombreAndTipoEstado(String nombre, String tipoEstado);
} 