package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "impuesto_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpuestoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_comprobante_id", nullable = false)
    private DetalleComprobante detalleComprobante;

    @Column(name = "codigo_impuesto", nullable = false, length = 2)
    private String codigoImpuesto;

    @Column(name = "codigo_porcentaje", nullable = false, length = 2)
    private String codigoPorcentaje;

    @Column(name = "tarifa", nullable = false, precision = 6, scale = 2)
    private BigDecimal tarifa;

    @Column(name = "base_imponible", nullable = false, precision = 14, scale = 2)
    private BigDecimal baseImponible;

    @Column(name = "valor", nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;
}
