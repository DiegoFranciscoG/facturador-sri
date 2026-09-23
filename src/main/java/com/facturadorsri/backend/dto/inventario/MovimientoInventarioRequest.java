package com.facturadorsri.backend.dto.inventario;

import com.facturadorsri.backend.domain.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MovimientoInventarioRequest(
        @NotNull Long productoId,
        @NotNull Long bodegaId,
        @NotNull TipoMovimientoInventario tipoMovimiento,

        @NotNull
        @DecimalMin(value = "0.000001", message = "la cantidad debe ser mayor a cero")
        @DecimalMax(value = "999999.999999", message = "la cantidad es demasiado alta")
        @Digits(integer = 6, fraction = 6, message = "maximo 6 decimales")
        BigDecimal cantidad,

        @DecimalMin(value = "0.0", message = "el costo no puede ser negativo")
        @DecimalMax(value = "999999.999999", message = "el costo es demasiado alto")
        @Digits(integer = 6, fraction = 6, message = "maximo 6 decimales")
        BigDecimal costoUnitario,

        @Size(max = 300)
        String observacion
) {
}
