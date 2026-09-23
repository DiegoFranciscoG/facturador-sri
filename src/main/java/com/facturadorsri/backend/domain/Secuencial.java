package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "secuencial")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Secuencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "punto_emision_id", nullable = false)
    private PuntoEmision puntoEmision;

    @Column(name = "tipo_comprobante", nullable = false, length = 2)
    private String tipoComprobante;

    @Column(name = "ultimo_numero", nullable = false)
    private long ultimoNumero;
}
