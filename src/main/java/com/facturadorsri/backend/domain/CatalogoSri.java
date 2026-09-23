package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "catalogo_sri")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoSri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "catalogo", nullable = false, length = 50)
    private String catalogo;

    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "valor_numerico", precision = 6, scale = 2)
    private BigDecimal valorNumerico;

    @Column(name = "version_ficha_tecnica", nullable = false, length = 10)
    private String versionFichaTecnica;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;
}
