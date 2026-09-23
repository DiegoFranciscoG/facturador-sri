package com.facturadorsri.backend.dto.comprobante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NotaCreditoRequest(
        @NotNull Long comprobanteOriginalId,
        @NotNull Long bodegaId,
        @NotBlank @Size(max = 300) String motivo,
        @NotEmpty @Valid List<VentaLineaRequest> lineas
) {
}
