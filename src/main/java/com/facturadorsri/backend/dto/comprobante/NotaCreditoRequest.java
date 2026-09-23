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
        @NotBlank @Size(min = 5, max = 300, message = "el motivo debe tener entre 5 y 300 caracteres") String motivo,
        @NotEmpty @Size(max = 200, message = "maximo 200 lineas por comprobante") @Valid List<VentaLineaRequest> lineas
) {
}
