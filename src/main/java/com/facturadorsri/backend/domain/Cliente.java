package com.facturadorsri.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_identificacion", nullable = false, length = 2)
    private String tipoIdentificacion;

    @Column(name = "identificacion", nullable = false, length = 20)
    private String identificacion;

    @Column(name = "razon_social", nullable = false, length = 300)
    private String razonSocial;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void alCrear() {
        createdAt = LocalDateTime.now();
    }
}
