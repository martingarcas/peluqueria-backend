package com.jve.Repository;

import com.jve.Entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Integer id);
    Optional<Servicio> findByNombre(String nombre);
    List<Servicio> findByUsuariosId(Integer trabajadorId);
} 