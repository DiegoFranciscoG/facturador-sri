-- Configuración del emisor y su estructura de facturación (establecimientos,
-- puntos de emisión y secuenciales). Ver docs/modelo-datos.md.

CREATE TABLE emisor (
    id                        BIGSERIAL PRIMARY KEY,
    ruc                       VARCHAR(13) NOT NULL UNIQUE,
    razon_social              VARCHAR(300) NOT NULL,
    nombre_comercial          VARCHAR(300),
    direccion_matriz          VARCHAR(300) NOT NULL,
    contribuyente_especial    VARCHAR(20),
    obligado_contabilidad     BOOLEAN NOT NULL DEFAULT FALSE,
    regimen_rimpe             VARCHAR(40),
    agente_retencion          VARCHAR(20),
    ambiente                  VARCHAR(1) NOT NULL DEFAULT '1',
    created_at                TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_emisor_ruc CHECK (ruc ~ '^[0-9]{13}$'),
    CONSTRAINT chk_emisor_ambiente CHECK (ambiente IN ('1', '2')),
    CONSTRAINT chk_emisor_regimen_rimpe CHECK (
        regimen_rimpe IS NULL OR regimen_rimpe IN (
            'CONTRIBUYENTE_NEGOCIO_POPULAR', 'CONTRIBUYENTE_REGIMEN_RIMPE'
        )
    )
);

CREATE TABLE establecimiento (
    id             BIGSERIAL PRIMARY KEY,
    emisor_id      BIGINT NOT NULL REFERENCES emisor (id),
    codigo         VARCHAR(3) NOT NULL,
    direccion      VARCHAR(300) NOT NULL,
    nombre_comercial VARCHAR(300),
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_establecimiento UNIQUE (emisor_id, codigo),
    CONSTRAINT chk_establecimiento_codigo CHECK (codigo ~ '^[0-9]{3}$')
);

CREATE TABLE punto_emision (
    id                  BIGSERIAL PRIMARY KEY,
    establecimiento_id  BIGINT NOT NULL REFERENCES establecimiento (id),
    codigo              VARCHAR(3) NOT NULL,
    descripcion         VARCHAR(200) NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_punto_emision UNIQUE (establecimiento_id, codigo),
    CONSTRAINT chk_punto_emision_codigo CHECK (codigo ~ '^[0-9]{3}$')
);

-- Un secuencial por punto de emisión y tipo de comprobante. La asignación
-- del siguiente número se hace con SELECT ... FOR UPDATE sobre esta fila
-- (bloqueo pesimista) dentro de la transacción que crea el comprobante,
-- para no duplicar secuenciales bajo concurrencia.
CREATE TABLE secuencial (
    id                 BIGSERIAL PRIMARY KEY,
    punto_emision_id   BIGINT NOT NULL REFERENCES punto_emision (id),
    tipo_comprobante   VARCHAR(2) NOT NULL,
    ultimo_numero      BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_secuencial UNIQUE (punto_emision_id, tipo_comprobante),
    CONSTRAINT chk_secuencial_max CHECK (ultimo_numero >= 0 AND ultimo_numero <= 999999999)
);
