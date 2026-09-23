package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Column(name = "forma_pago", nullable = false, length = 2)
    private String formaPago;

    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "plazo")
    private Integer plazo;

    @Column(name = "unidad_tiempo", length = 20)
    private String unidadTiempo;
}
