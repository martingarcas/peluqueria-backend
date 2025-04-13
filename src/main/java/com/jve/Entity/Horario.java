package com.jve.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Time;
import java.util.Set;

@Entity
@Data
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String nombre;
    
    @Enumerated(EnumType.STRING)
    private DiaSemana diaSemana;
    
    private Time horaInicio;
    private Time horaFin;
    
    @ManyToMany
    @JoinTable(
        name = "horario_trabajador",
        joinColumns = @JoinColumn(name = "horario_id"),
        inverseJoinColumns = @JoinColumn(name = "trabajador_id")
    )
    private Set<Usuario> trabajadores;
} 