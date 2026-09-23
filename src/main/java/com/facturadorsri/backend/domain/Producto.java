package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_principal", nullable = false, unique = true, length = 25)
    private String codigoPrincipal;

    @Column(name = "codigo_auxiliar", length = 25)
    private String codigoAuxiliar;

    @Column(name = "descripcion", nullable = false, length = 300)
    private String descripcion;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 6)
    private BigDecimal precioUnitario;

    @Column(name = "codigo_impuesto", nullable = false, length = 2)
    private String codigoImpuesto;

    @Column(name = "codigo_porcentaje_iva", nullable = false, length = 2)
    private String codigoPorcentajeIva;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        createdAt = ahora;
        updatedAt = ahora;
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = LocalDateTime.now();
    }
}
