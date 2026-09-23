package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.PuntoEmision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PuntoEmisionRepository extends JpaRepository<PuntoEmision, Long> {
    Optional<PuntoEmision> findByEstablecimientoIdAndCodigo(Long establecimientoId, String codigo);
}
