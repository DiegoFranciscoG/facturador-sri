package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "establecimiento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emisor_id", nullable = false)
    private Emisor emisor;

    @Column(name = "codigo", nullable = false, length = 3)
    private String codigo;

    @Column(name = "direccion", nullable = false, length = 300)
    private String direccion;

    @Column(name = "nombre_comercial", length = 300)
    private String nombreComercial;

    @Column(name = "activo", nullable = false)
    private boolean activo;
}
