package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoPrincipal(String codigoPrincipal);
    boolean existsByCodigoPrincipal(String codigoPrincipal);
}
