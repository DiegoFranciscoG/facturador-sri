package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "detalle_comprobante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "codigo_principal", nullable = false, length = 25)
    private String codigoPrincipal;

    @Column(name = "descripcion", nullable = false, length = 300)
    private String descripcion;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 6)
    private BigDecimal precioUnitario;

    @Column(name = "descuento", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "precio_total_sin_impuesto", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioTotalSinImpuesto;

    @OneToMany(mappedBy = "detalleComprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImpuestoDetalle> impuestos = new ArrayList<>();

    public void agregarImpuesto(ImpuestoDetalle impuesto) {
        impuestos.add(impuesto);
        impuesto.setDetalleComprobante(this);
    }
}
