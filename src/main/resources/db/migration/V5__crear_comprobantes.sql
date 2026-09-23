-- Comprobantes electrónicos y sus detalles. clave_acceso es la clave de
-- idempotencia (ver docs/modelo-datos.md sec. 2): un mismo comprobante no
-- se procesa dos veces contra el SRI aunque se reintente el envío.

CREATE TABLE comprobante (
    id                        BIGSERIAL PRIMARY KEY,
    clave_acceso              CHAR(49) NOT NULL UNIQUE,
    tipo_comprobante          VARCHAR(2) NOT NULL,
    establecimiento_id        BIGINT NOT NULL REFERENCES establecimiento (id),
    punto_emision_id          BIGINT NOT NULL REFERENCES punto_emision (id),
    secuencial                VARCHAR(9) NOT NULL,
    fecha_emision             DATE NOT NULL,
    ambiente                  CHAR(1) NOT NULL,
    tipo_emision              CHAR(1) NOT NULL DEFAULT '1',
    cliente_id                BIGINT NOT NULL REFERENCES cliente (id),
    comprobante_relacionado_id BIGINT REFERENCES comprobante (id),
    motivo                    VARCHAR(300),
    estado                    VARCHAR(20) NOT NULL DEFAULT 'GENERADO',
    total_sin_impuestos       NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_descuento           NUMERIC(14, 2) NOT NULL DEFAULT 0,
    importe_total             NUMERIC(14, 2) NOT NULL DEFAULT 0,
    xml_firmado               TEXT,
    xml_autorizacion          TEXT,
    numero_autorizacion       VARCHAR(49),
    fecha_autorizacion        TIMESTAMP,
    intentos_envio            INTEGER NOT NULL DEFAULT 0,
    proximo_reintento_en      TIMESTAMP,
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_comprobante_secuencial UNIQUE (
        establecimiento_id, punto_emision_id, tipo_comprobante, secuencial
    ),
    CONSTRAINT chk_comprobante_estado CHECK (estado IN (
        'GENERADO', 'FIRMADO', 'RECIBIDO', 'DEVUELTA', 'AUTORIZADO', 'NO_AUTORIZADO'
    )),
    CONSTRAINT chk_comprobante_ambiente CHECK (ambiente IN ('1', '2'))
);

CREATE INDEX idx_comprobante_estado ON comprobante (estado);
CREATE INDEX idx_comprobante_cliente ON comprobante (cliente_id);

ALTER TABLE movimiento_inventario
    ADD CONSTRAINT fk_movimiento_comprobante
    FOREIGN KEY (comprobante_id) REFERENCES comprobante (id);

CREATE TABLE detalle_comprobante (
    id                          BIGSERIAL PRIMARY KEY,
    comprobante_id              BIGINT NOT NULL REFERENCES comprobante (id),
    producto_id                 BIGINT REFERENCES producto (id),
    codigo_principal            VARCHAR(25) NOT NULL,
    descripcion                 VARCHAR(300) NOT NULL,
    cantidad                    NUMERIC(12, 6) NOT NULL,
    precio_unitario             NUMERIC(12, 6) NOT NULL,
    descuento                   NUMERIC(12, 2) NOT NULL DEFAULT 0,
    precio_total_sin_impuesto   NUMERIC(14, 2) NOT NULL
);

CREATE INDEX idx_detalle_comprobante ON detalle_comprobante (comprobante_id);

CREATE TABLE impuesto_detalle (
    id                       BIGSERIAL PRIMARY KEY,
    detalle_comprobante_id   BIGINT NOT NULL REFERENCES detalle_comprobante (id),
    codigo_impuesto          VARCHAR(2) NOT NULL,
    codigo_porcentaje        VARCHAR(2) NOT NULL,
    tarifa                   NUMERIC(6, 2) NOT NULL,
    base_imponible           NUMERIC(14, 2) NOT NULL,
    valor                    NUMERIC(14, 2) NOT NULL
);

CREATE TABLE pago (
    id               BIGSERIAL PRIMARY KEY,
    comprobante_id   BIGINT NOT NULL REFERENCES comprobante (id),
    forma_pago       VARCHAR(2) NOT NULL,
    total            NUMERIC(14, 2) NOT NULL,
    plazo            INTEGER,
    unidad_tiempo    VARCHAR(20)
);

CREATE INDEX idx_pago_comprobante ON pago (comprobante_id);

-- Auditoría de cada interacción con los web services del SRI (recepción y
-- autorización), incluyendo reintentos.
CREATE TABLE evento_sri (
    id                  BIGSERIAL PRIMARY KEY,
    comprobante_id      BIGINT NOT NULL REFERENCES comprobante (id),
    tipo_evento         VARCHAR(30) NOT NULL,
    request_payload     TEXT,
    response_payload    TEXT,
    estado_resultante   VARCHAR(20),
    mensaje_error       TEXT,
    intento_numero      INTEGER NOT NULL DEFAULT 1,
    fecha               TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_evento_tipo CHECK (tipo_evento IN (
        'ENVIO_RECEPCION', 'RESPUESTA_RECEPCION',
        'ENVIO_AUTORIZACION', 'RESPUESTA_AUTORIZACION'
    ))
);

CREATE INDEX idx_evento_sri_comprobante ON evento_sri (comprobante_id);
