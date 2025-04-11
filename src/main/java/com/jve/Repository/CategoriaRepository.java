package com.jve.Repository;

import com.jve.Entity.Categoria;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    boolean existsByNombre(String nombre);
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
} 