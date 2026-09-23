-- Bodegas y kardex (movimientos_inventario). El saldo de cada producto por
-- bodega se deriva del último movimiento (saldo_cantidad), no de una tabla
-- de existencias aparte, para no tener dos fuentes de verdad.

CREATE TABLE bodega (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    establecimiento_id  BIGINT REFERENCES establecimiento (id),
    activa              BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE movimiento_inventario (
    id                BIGSERIAL PRIMARY KEY,
    producto_id       BIGINT NOT NULL REFERENCES producto (id),
    bodega_id         BIGINT NOT NULL REFERENCES bodega (id),
    tipo_movimiento   VARCHAR(20) NOT NULL,
    cantidad          NUMERIC(12, 6) NOT NULL,
    costo_unitario    NUMERIC(12, 6),
    saldo_cantidad    NUMERIC(12, 6) NOT NULL,
    comprobante_id    BIGINT,
    fecha             TIMESTAMP NOT NULL DEFAULT now(),
    observacion       VARCHAR(300),
    CONSTRAINT chk_movimiento_tipo CHECK (tipo_movimiento IN (
        'ENTRADA_COMPRA', 'SALIDA_VENTA', 'AJUSTE_POSITIVO',
        'AJUSTE_NEGATIVO', 'DEVOLUCION_CLIENTE', 'DEVOLUCION_PROVEEDOR'
    )),
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_movimiento_producto_bodega
    ON movimiento_inventario (producto_id, bodega_id, fecha);
