package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {
    Optional<Establecimiento> findByCodigo(String codigo);
}
