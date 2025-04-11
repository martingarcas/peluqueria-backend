package com.jve.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "estado", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre", "tipo_estado"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "tipo_estado", nullable = false)
    private String tipoEstado; // CITA, CONTRATO, PEDIDO

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference
    private List<Cita> citas = new ArrayList<>();

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference
    private List<Contrato> contratos = new ArrayList<>();

    // Constructor para crear estados fácilmente
    public Estado(String nombre, String tipoEstado) {
        this.nombre = nombre;
        this.tipoEstado = tipoEstado;
    }
} 