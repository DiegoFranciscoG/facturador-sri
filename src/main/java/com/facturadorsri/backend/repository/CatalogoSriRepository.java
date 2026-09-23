package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.CatalogoSri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CatalogoSriRepository extends JpaRepository<CatalogoSri, Long> {

    List<CatalogoSri> findByCatalogo(String catalogo);

    @Query("""
            select c from CatalogoSri c
            where c.catalogo = :catalogo and c.codigo = :codigo
            and c.vigenteDesde <= :fecha
            and (c.vigenteHasta is null or c.vigenteHasta >= :fecha)
            """)
    Optional<CatalogoSri> buscarVigente(@Param("catalogo") String catalogo,
                                         @Param("codigo") String codigo,
                                         @Param("fecha") LocalDate fecha);
}
