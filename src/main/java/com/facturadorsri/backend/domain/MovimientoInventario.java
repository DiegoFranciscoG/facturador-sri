package com.facturadorsri.backend.domain;

import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimientoInventario tipoMovimiento;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", precision = 12, scale = 6)
    private BigDecimal costoUnitario;

    @Column(name = "saldo_cantidad", nullable = false, precision = 12, scale = 6)
    private BigDecimal saldoCantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @PrePersist
    void alCrear() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
