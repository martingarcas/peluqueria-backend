package com.jve.Repository;

import com.jve.Entity.DiaSemana;
import com.jve.Entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.sql.Time;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    
    List<Horario> findByTrabajadoresId(Integer trabajadorId);
    
    boolean existsByNombre(String nombre);
    
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Horario h " +
           "JOIN h.trabajadores t " +
           "WHERE t.id = :trabajadorId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.horaInicio < :horaFin " +
           "AND h.horaFin > :horaInicio")
    boolean existsByTrabajadoresAndDiaSemanaAndHoraInicioLessThanAndHoraFinGreaterThan(
        @Param("trabajadorId") Integer trabajadorId, 
        @Param("diaSemana") DiaSemana diaSemana, 
        @Param("horaFin") Time horaFin, 
        @Param("horaInicio") Time horaInicio);
        
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Horario h " +
           "JOIN h.trabajadores t " +
           "WHERE t.id = :trabajadorId " +
           "AND h.diaSemana = :diaSemana " +
           "AND h.horaInicio < :horaFin " +
           "AND h.horaFin > :horaInicio " +
           "AND h.id != :horarioId")
    boolean existsByTrabajadoresAndDiaSemanaAndHoraInicioLessThanAndHoraFinGreaterThanAndIdNot(
        @Param("trabajadorId") Integer trabajadorId, 
        @Param("diaSemana") DiaSemana diaSemana, 
        @Param("horaFin") Time horaFin, 
        @Param("horaInicio") Time horaInicio, 
        @Param("horarioId") Integer horarioId);
} 