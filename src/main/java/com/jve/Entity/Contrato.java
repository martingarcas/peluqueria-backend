package com.jve.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    @Column(name = "fecha_inicio_contrato")
    @Temporal(TemporalType.DATE)
    private Date fechaInicioContrato;

    @Column(name = "fecha_fin_contrato")
    @Temporal(TemporalType.DATE)
    private Date fechaFinContrato;

    @Column(name = "tipo_contrato", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoContrato tipoContrato;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    @JsonBackReference
    private Estado estado;

    @Column(name = "url_contrato")
    private String urlContrato;
} 