package com.facturadorsri.backend.domain.enums;

import java.util.Arrays;

/** Tabla 24 de la Ficha Tecnica: formas de pago (docs/investigacion.md 4.7). */
public enum FormaPago {

    SIN_UTILIZACION_SISTEMA_FINANCIERO("01"),
    COMPENSACION_DEUDAS("15"),
    TARJETA_DEBITO("16"),
    DINERO_ELECTRONICO("17"),
    TARJETA_PREPAGO("18"),
    TARJETA_CREDITO("19"),
    OTROS_CON_UTILIZACION_SISTEMA_FINANCIERO("20"),
    ENDOSO_TITULOS("21");

    private final String codigo;

    FormaPago(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static FormaPago deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(f -> f.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Codigo de forma de pago desconocido: " + codigo));
    }
}
