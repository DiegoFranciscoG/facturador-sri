package com.facturadorsri.backend.service;

import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.repository.ComprobanteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cola de reintentos ante fallas de red o respuestas "EN PROCESAMIENTO" /
 * "DEVUELTA" del SRI. Corre cada minuto y retoma cualquier comprobante cuyo
 * proximo_reintento_en ya paso (ver ComprobanteProcesamientoService).
 */
@Service
@RequiredArgsConstructor
public class ReintentoSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ReintentoSchedulerService.class);

    private final ComprobanteRepository comprobanteRepository;
    private final ComprobanteProcesamientoService comprobanteProcesamientoService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void reintentarPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Comprobante> pendientesRecibido = comprobanteRepository
                .findByEstadoAndProximoReintentoEnLessThanEqual(EstadoComprobante.RECIBIDO, ahora);
        List<Comprobante> pendientesDevueltos = comprobanteRepository
                .findByEstadoAndProximoReintentoEnLessThanEqual(EstadoComprobante.DEVUELTA, ahora);

        for (Comprobante comprobante : pendientesRecibido) {
            reintentar(comprobante);
        }
        for (Comprobante comprobante : pendientesDevueltos) {
            reintentar(comprobante);
        }
    }

    private void reintentar(Comprobante comprobante) {
        try {
            comprobanteProcesamientoService.procesar(comprobante.getId());
        } catch (Exception e) {
            log.warn("Reintento fallido para el comprobante {}: {}", comprobante.getClaveAcceso(), e.getMessage());
        }
    }
}
