package com.jve.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fecha;

    @Column(name = "hora_inicio", nullable = false)
    @Temporal(TemporalType.TIME)
    private Date horaInicio;

    @Column(name = "hora_fin", nullable = false)
    @Temporal(TemporalType.TIME)
    private Date horaFin;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    @JsonBackReference
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_trabajador", nullable = false)
    @JsonBackReference
    private Usuario trabajador;

    @OneToMany(mappedBy = "cita")
    @JsonManagedReference
    private List<CitaServicio> citasServicios = new ArrayList<>();
} 