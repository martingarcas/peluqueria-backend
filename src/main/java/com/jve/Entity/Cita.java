package com.jve.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.sql.Time;

@Entity
@Table(name = "cita")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "id_trabajador", nullable = false)
    private Usuario trabajador;
    
    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;
    
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @Column(name = "hora_inicio", nullable = false)
    private Time horaInicio;
    
    @Column(name = "hora_fin", nullable = false)
    private Time horaFin;
    
    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;
} 