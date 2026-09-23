package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.Emisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmisorRepository extends JpaRepository<Emisor, Long> {
    Optional<Emisor> findByRuc(String ruc);
}
