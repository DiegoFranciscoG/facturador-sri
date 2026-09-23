package com.facturadorsri.backend.domain;

import com.facturadorsri.backend.domain.enums.RegimenRimpe;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "emisor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruc", nullable = false, unique = true, length = 13)
    private String ruc;

    @Column(name = "razon_social", nullable = false, length = 300)
    private String razonSocial;

    @Column(name = "nombre_comercial", length = 300)
    private String nombreComercial;

    @Column(name = "direccion_matriz", nullable = false, length = 300)
    private String direccionMatriz;

    @Column(name = "contribuyente_especial", length = 20)
    private String contribuyenteEspecial;

    @Column(name = "obligado_contabilidad", nullable = false)
    private boolean obligadoContabilidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "regimen_rimpe", length = 40)
    private RegimenRimpe regimenRimpe;

    @Column(name = "agente_retencion", length = 20)
    private String agenteRetencion;

    @Column(name = "ambiente", nullable = false, length = 1)
    private String ambiente;

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
