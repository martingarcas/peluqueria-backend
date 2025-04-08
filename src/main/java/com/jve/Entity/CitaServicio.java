package com.jve.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "cita_servicio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CitaServicio {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_cita")
    @JsonManagedReference
    private Cita cita;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_servicio")
    @JsonManagedReference
    private Servicio servicio;
} 