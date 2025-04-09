package com.jve.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jve.Entity.Cita;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByUsuarioId(Integer idUsuario);
    List<Cita> findByCitasServiciosServicioId(Integer idServicio);
} 