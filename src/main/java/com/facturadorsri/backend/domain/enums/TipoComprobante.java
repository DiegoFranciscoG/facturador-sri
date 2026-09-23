package com.facturadorsri.backend.domain.enums;

import java.util.Arrays;

/**
 * Tabla 3 de la Ficha Tecnica de Comprobantes Electronicos (docs/investigacion.md 4.1).
 * No existe el codigo "02": es un salto documentado desde versiones antiguas de la ficha.
 */
public enum TipoComprobante {

    FACTURA("01"),
    LIQUIDACION_COMPRA("03"),
    NOTA_CREDITO("04"),
    NOTA_DEBITO("05"),
    GUIA_REMISION("06"),
    RETENCION("07");

    private final String codigo;

    TipoComprobante(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static TipoComprobante deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(t -> t.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Codigo de tipo de comprobante desconocido: " + codigo));
    }
}
