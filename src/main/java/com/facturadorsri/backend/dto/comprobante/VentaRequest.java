package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VentaRequest(
        @NotNull Long establecimientoId,
        @NotNull Long puntoEmisionId,
        @NotNull Long clienteId,
        @NotNull Long bodegaId,

        @NotEmpty @Size(max = 200, message = "maximo 200 lineas por comprobante")
        @Valid List<VentaLineaRequest> lineas,

        @NotEmpty @Size(max = 20, message = "maximo 20 formas de pago")
        @Valid List<PagoRequest> pagos
) {
}
