-- Usuarios del backend (login JWT). El usuario administrador inicial se crea
-- en el arranque desde ADMIN_USERNAME/ADMIN_PASSWORD (variables de entorno),
-- nunca con una contraseña de ejemplo versionada. Ver SeguridadBootstrapRunner.

CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    rol             VARCHAR(20) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN', 'CAJERO'))
);
