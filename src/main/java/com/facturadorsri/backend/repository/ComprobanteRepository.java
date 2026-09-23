package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    Optional<Comprobante> findByClaveAcceso(String claveAcceso);

    boolean existsByClaveAcceso(String claveAcceso);

    Page<Comprobante> findByEstado(EstadoComprobante estado, Pageable pageable);

    Page<Comprobante> findByClienteId(Long clienteId, Pageable pageable);

    List<Comprobante> findByEstadoAndProximoReintentoEnLessThanEqual(EstadoComprobante estado, LocalDateTime instante);
}
