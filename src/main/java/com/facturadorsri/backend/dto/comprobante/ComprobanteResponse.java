package com.facturadorsri.backend.dto.comprobante;

import com.facturadorsri.backend.domain.enums.EstadoComprobante;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ComprobanteResponse(
        Long id,
        String claveAcceso,
        String tipoComprobante,
        String secuencial,
        LocalDate fechaEmision,
        EstadoComprobante estado,
        BigDecimal totalSinImpuestos,
        BigDecimal totalDescuento,
        BigDecimal importeTotal,
        String numeroAutorizacion,
        LocalDateTime fechaAutorizacion,
        String clienteRazonSocial
) {
}
