package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByProductoIdAndBodegaIdOrderByFechaDesc(Long productoId, Long bodegaId);

    Optional<MovimientoInventario> findFirstByProductoIdAndBodegaIdOrderByIdDesc(Long productoId, Long bodegaId);
}
