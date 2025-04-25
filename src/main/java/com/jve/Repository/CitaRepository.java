package com.jve.Repository;

import com.jve.Entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Date;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByUsuarioId(Integer idUsuario);
    
    List<Cita> findByTrabajadorId(Integer idTrabajador);
    
    @Query("SELECT c FROM Cita c WHERE " +
           "((:esParaTrabajador = true AND c.trabajador.id = :usuarioId) OR " +
           "(:esParaTrabajador = false AND c.usuario.id = :usuarioId)) AND " +
           "c.fecha = :fecha AND " +
           "CAST(c.horaInicio AS time) < CAST(:horaFin AS time) AND " +
           "CAST(c.horaFin AS time) > CAST(:horaInicio AS time)")
    List<Cita> findSolapadas(@Param("usuarioId") Integer usuarioId,
                            @Param("fecha") Date fecha,
                            @Param("horaInicio") Date horaInicio,
                            @Param("horaFin") Date horaFin,
                            @Param("esParaTrabajador") boolean esParaTrabajador);

    List<Cita> findByFechaBetween(Date fechaInicio, Date fechaFin);
    
    List<Cita> findByTrabajadorIdAndFecha(Integer trabajadorId, Date fecha);

} 