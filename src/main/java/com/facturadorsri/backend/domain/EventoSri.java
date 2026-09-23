package com.facturadorsri.backend.domain;

import com.facturadorsri.backend.domain.enums.TipoEventoSri;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_sri")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoSri {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEventoSri tipoEvento;

    @Lob
    @Column(name = "request_payload")
    private String requestPayload;

    @Lob
    @Column(name = "response_payload")
    private String responsePayload;

    @Column(name = "estado_resultante", length = 20)
    private String estadoResultante;

    @Lob
    @Column(name = "mensaje_error")
    private String mensajeError;

    @Column(name = "intento_numero", nullable = false)
    @Builder.Default
    private int intentoNumero = 1;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void alCrear() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}
