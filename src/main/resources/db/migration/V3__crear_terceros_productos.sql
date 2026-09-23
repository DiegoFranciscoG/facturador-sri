-- Clientes y productos. Los códigos de catálogo referenciados
-- (tipo_identificacion, codigo_impuesto, codigo_porcentaje_iva) se validan
-- en la capa de servicio contra catalogo_sri, no con FK física, porque el
-- mismo código puede repetirse en distintas versiones de la ficha técnica.

CREATE TABLE cliente (
    id                  BIGSERIAL PRIMARY KEY,
    tipo_identificacion VARCHAR(2) NOT NULL,
    identificacion      VARCHAR(20) NOT NULL,
    razon_social        VARCHAR(300) NOT NULL,
    direccion           VARCHAR(300),
    email               VARCHAR(150),
    telefono            VARCHAR(20),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_cliente_identificacion UNIQUE (tipo_identificacion, identificacion)
);

CREATE TABLE producto (
    id                      BIGSERIAL PRIMARY KEY,
    codigo_principal        VARCHAR(25) NOT NULL UNIQUE,
    codigo_auxiliar         VARCHAR(25),
    descripcion             VARCHAR(300) NOT NULL,
    unidad_medida           VARCHAR(20) NOT NULL DEFAULT 'UNIDAD',
    precio_unitario         NUMERIC(12, 6) NOT NULL,
    codigo_impuesto         VARCHAR(2) NOT NULL DEFAULT '2',
    codigo_porcentaje_iva   VARCHAR(2) NOT NULL,
    activo                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_producto_precio CHECK (precio_unitario >= 0)
);
