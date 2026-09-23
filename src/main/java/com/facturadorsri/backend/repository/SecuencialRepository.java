package com.facturadorsri.backend.repository;

import com.facturadorsri.backend.domain.Secuencial;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SecuencialRepository extends JpaRepository<Secuencial, Long> {

    /**
     * Bloqueo pesimista (SELECT ... FOR UPDATE): dos ventas concurrentes en el
     * mismo punto de emision y tipo de comprobante nunca pueden leer el mismo
     * ultimo_numero. Debe invocarse dentro de una transaccion (@Transactional)
     * que tambien cree el Comprobante, para que el lock se mantenga hasta el commit.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Secuencial s where s.puntoEmision.id = :puntoEmisionId and s.tipoComprobante = :tipoComprobante")
    Optional<Secuencial> buscarParaActualizar(@Param("puntoEmisionId") Long puntoEmisionId,
                                               @Param("tipoComprobante") String tipoComprobante);
}
