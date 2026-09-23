package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VentaRequest(
        @NotNull Long establecimientoId,
        @NotNull Long puntoEmisionId,
        @NotNull Long clienteId,
        @NotNull Long bodegaId,
        @NotEmpty @Valid List<VentaLineaRequest> lineas,
        @NotEmpty @Valid List<PagoRequest> pagos
) {
}
