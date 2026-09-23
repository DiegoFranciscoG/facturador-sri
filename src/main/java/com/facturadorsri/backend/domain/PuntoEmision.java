package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "punto_emision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntoEmision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @Column(name = "codigo", nullable = false, length = 3)
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
