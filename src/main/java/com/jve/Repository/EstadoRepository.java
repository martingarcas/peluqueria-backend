package com.jve.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jve.Entity.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    boolean existsByNombre(String nombre);
} 