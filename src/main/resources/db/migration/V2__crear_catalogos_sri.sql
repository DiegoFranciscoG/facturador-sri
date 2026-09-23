-- Catálogos oficiales de la Ficha Técnica de Comprobantes Electrónicos,
-- versionados para poder auditar contra qué versión se validó cada
-- comprobante histórico. Ver docs/investigacion.md seccion 4.

CREATE TABLE catalogo_sri (
    id                      BIGSERIAL PRIMARY KEY,
    catalogo                VARCHAR(50) NOT NULL,
    codigo                  VARCHAR(10) NOT NULL,
    descripcion             VARCHAR(200) NOT NULL,
    valor_numerico          NUMERIC(6, 2),
    version_ficha_tecnica   VARCHAR(10) NOT NULL,
    vigente_desde           DATE NOT NULL,
    vigente_hasta           DATE,
    CONSTRAINT uk_catalogo_sri UNIQUE (catalogo, codigo, version_ficha_tecnica),
    CONSTRAINT chk_catalogo_sri_vigencia CHECK (
        vigente_hasta IS NULL OR vigente_hasta >= vigente_desde
    )
);

CREATE INDEX idx_catalogo_sri_lookup ON catalogo_sri (catalogo, codigo);
