package com.facturadorsri.backend.dto.inventario;

import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long id,
        Long productoId,
        Long bodegaId,
        TipoMovimientoInventario tipoMovimiento,
        BigDecimal cantidad,
        BigDecimal costoUnitario,
        BigDecimal saldoCantidad,
        Long comprobanteId,
        LocalDateTime fecha,
        String observacion
) {
}
