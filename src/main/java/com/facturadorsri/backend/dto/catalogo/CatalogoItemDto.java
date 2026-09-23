package com.facturadorsri.backend.dto.catalogo;

import java.math.BigDecimal;

public record CatalogoItemDto(String codigo, String descripcion, BigDecimal valorNumerico) {
}
