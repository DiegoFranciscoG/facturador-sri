package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.PuntoEmision;
import com.facturadorsri.backend.domain.Secuencial;
import com.facturadorsri.backend.exception.BusinessException;
import com.facturadorsri.backend.exception.ResourceNotFoundException;
import com.facturadorsri.backend.repository.PuntoEmisionRepository;
import com.facturadorsri.backend.repository.SecuencialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asigna el siguiente numero de secuencial con bloqueo pesimista
 * (SELECT ... FOR UPDATE), para que dos ventas concurrentes en el mismo
 * punto de emision y tipo de comprobante nunca obtengan el mismo numero.
 * Debe llamarse SIEMPRE dentro de la transaccion que crea el Comprobante
 * (Propagation.MANDATORY) para que el lock se mantenga hasta el commit.
 */
@Service
@RequiredArgsConstructor
public class SecuencialService {

    private static final long MAXIMO = 999_999_999L;

    private final SecuencialRepository secuencialRepository;
    private final PuntoEmisionRepository puntoEmisionRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguienteNumero(Long puntoEmisionId, String tipoComprobante) {
        Secuencial secuencial = secuencialRepository.buscarParaActualizar(puntoEmisionId, tipoComprobante)
                .orElseGet(() -> crear(puntoEmisionId, tipoComprobante));

        long siguiente = secuencial.getUltimoNumero() + 1;
        if (siguiente > MAXIMO) {
            throw new BusinessException(
                    "El secuencial del punto de emision para el tipo %s esta agotado".formatted(tipoComprobante));
        }
        secuencial.setUltimoNumero(siguiente);
        secuencialRepository.save(secuencial);
        return "%09d".formatted(siguiente);
    }

    private Secuencial crear(Long puntoEmisionId, String tipoComprobante) {
        PuntoEmision puntoEmision = puntoEmisionRepository.findById(puntoEmisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Punto de emision no encontrado: " + puntoEmisionId));
        Secuencial nuevo = Secuencial.builder()
                .puntoEmision(puntoEmision)
                .tipoComprobante(tipoComprobante)
                .ultimoNumero(0)
                .build();
        return secuencialRepository.save(nuevo);
    }
}
