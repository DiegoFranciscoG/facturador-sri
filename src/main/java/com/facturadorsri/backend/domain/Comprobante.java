package com.facturadorsri.backend.domain;

import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comprobante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave_acceso", nullable = false, unique = true, length = 49)
    private String claveAcceso;

    @Column(name = "tipo_comprobante", nullable = false, length = 2)
    private String tipoComprobante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "punto_emision_id", nullable = false)
    private PuntoEmision puntoEmision;

    @Column(name = "secuencial", nullable = false, length = 9)
    private String secuencial;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "ambiente", nullable = false, length = 1)
    private String ambiente;

    @Column(name = "tipo_emision", nullable = false, length = 1)
    @Builder.Default
    private String tipoEmision = "1";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_relacionado_id")
    private Comprobante comprobanteRelacionado;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoComprobante estado = EstadoComprobante.GENERADO;

    @Column(name = "total_sin_impuestos", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalSinImpuestos = BigDecimal.ZERO;

    @Column(name = "total_descuento", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalDescuento = BigDecimal.ZERO;

    @Column(name = "importe_total", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal importeTotal = BigDecimal.ZERO;

    @Lob
    @Column(name = "xml_firmado")
    private String xmlFirmado;

    @Lob
    @Column(name = "xml_autorizacion")
    private String xmlAutorizacion;

    @Column(name = "numero_autorizacion", length = 49)
    private String numeroAutorizacion;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @Column(name = "intentos_envio", nullable = false)
    @Builder.Default
    private int intentosEnvio = 0;

    @Column(name = "proximo_reintento_en")
    private LocalDateTime proximoReintentoEn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleComprobante> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

    public void agregarDetalle(DetalleComprobante detalle) {
        detalles.add(detalle);
        detalle.setComprobante(this);
    }

    public void agregarPago(Pago pago) {
        pagos.add(pago);
        pago.setComprobante(this);
    }

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
