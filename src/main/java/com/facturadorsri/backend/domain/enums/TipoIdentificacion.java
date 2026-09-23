package com.facturadorsri.backend.domain.enums;

import java.util.Arrays;

/** Tabla 6 de la Ficha Tecnica (docs/investigacion.md 4.2). */
public enum TipoIdentificacion {

    RUC("04"),
    CEDULA("05"),
    PASAPORTE("06"),
    CONSUMIDOR_FINAL("07"),
    IDENTIFICACION_EXTERIOR("08");

    private final String codigo;

    TipoIdentificacion(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static TipoIdentificacion deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(t -> t.codigo.equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Codigo de tipo de identificacion desconocido: " + codigo));
    }
}
