package com.facturadorsri.backend.domain.enums;

public enum TipoMovimientoInventario {
    ENTRADA_COMPRA,
    SALIDA_VENTA,
    AJUSTE_POSITIVO,
    AJUSTE_NEGATIVO,
    DEVOLUCION_CLIENTE,
    DEVOLUCION_PROVEEDOR;

    public boolean esEntrada() {
        return this == ENTRADA_COMPRA || this == AJUSTE_POSITIVO || this == DEVOLUCION_CLIENTE;
    }
}
