package com.facturadorsri.backend.dto.inventario;

import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MovimientoInventarioRequest(
        @NotNull Long productoId,
        @NotNull Long bodegaId,
        @NotNull TipoMovimientoInventario tipoMovimiento,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal cantidad,
        BigDecimal costoUnitario,
        @Size(max = 300) String observacion
) {
}
