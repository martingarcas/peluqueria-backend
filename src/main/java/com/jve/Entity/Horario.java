package com.jve.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
    
    @ManyToMany(mappedBy = "horarios")
    @JsonBackReference
    private List<Usuario> trabajadores = new ArrayList<>();
} 