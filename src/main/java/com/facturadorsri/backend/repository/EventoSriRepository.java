package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.EventoSri;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoSriRepository extends JpaRepository<EventoSri, Long> {
    List<EventoSri> findByComprobanteIdOrderByFechaAsc(Long comprobanteId);
}
