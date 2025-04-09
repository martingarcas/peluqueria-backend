package com.jve.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jve.Entity.Servicio;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    boolean existsByNombre(String nombre);
    List<Servicio> findByIdEspecialidad(Integer idEspecialidad);
} 