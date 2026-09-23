package com.facturadorsri.backend.dto.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BodegaRequest(
        @NotBlank @Size(max = 150) String nombre,
        Long establecimientoId
) {
}
