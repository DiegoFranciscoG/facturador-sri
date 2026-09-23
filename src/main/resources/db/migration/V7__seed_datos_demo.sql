-- Datos FICTICIOS para desarrollo local. Ningún dato personal real.
-- El RUC y la cédula fueron calculados con los algoritmos de módulo 10/11
-- descritos en docs/investigacion.md seccion 5, por lo que tienen formato
-- válido aunque no correspondan a ninguna persona o empresa real.

INSERT INTO emisor (
    ruc, razon_social, nombre_comercial, direccion_matriz,
    obligado_contabilidad, regimen_rimpe, ambiente
) VALUES (
    '1790012344001', 'COMERCIAL DEMO CIA. LTDA.', 'DEMO STORE',
    'Av. República del Ficticio N34-56 y Calle Inventada, Quito',
    TRUE, 'CONTRIBUYENTE_REGIMEN_RIMPE', '1'
);

INSERT INTO establecimiento (emisor_id, codigo, direccion, nombre_comercial, activo)
VALUES (1, '001', 'Av. República del Ficticio N34-56 y Calle Inventada, Quito', 'DEMO STORE - Matriz', TRUE);

INSERT INTO punto_emision (establecimiento_id, codigo, descripcion, activo)
VALUES (1, '001', 'Caja principal', TRUE);

INSERT INTO secuencial (punto_emision_id, tipo_comprobante, ultimo_numero) VALUES
(1, '01', 0),
(1, '04', 0);

INSERT INTO bodega (nombre, establecimiento_id, activa)
VALUES ('Bodega principal', 1, TRUE);

INSERT INTO cliente (tipo_identificacion, identificacion, razon_social, direccion, email, telefono) VALUES
('05', '1725489635', 'CLIENTE DEMO PERSONA NATURAL', 'Calle Ficticia 123, Quito', 'demo.cliente@ejemplo.test', '0999999999'),
('07', '9999999999999', 'CONSUMIDOR FINAL', NULL, NULL, NULL);

INSERT INTO producto (
    codigo_principal, codigo_auxiliar, descripcion, unidad_medida,
    precio_unitario, codigo_impuesto, codigo_porcentaje_iva, activo
) VALUES
('PROD-001', 'AUX-001', 'Producto demo con IVA 15%', 'UNIDAD', 10.00, '2', '4', TRUE),
('PROD-002', 'AUX-002', 'Producto demo tarifa 0%', 'UNIDAD', 5.50, '2', '0', TRUE);

-- Stock inicial (kardex): sin esto los productos demo nacen con saldo 0 y
-- ninguna venta de prueba podria completarse.
INSERT INTO movimiento_inventario (
    producto_id, bodega_id, tipo_movimiento, cantidad, costo_unitario, saldo_cantidad, observacion
) VALUES
(1, 1, 'ENTRADA_COMPRA', 100, 6.00, 100, 'Stock inicial de demostración'),
(2, 1, 'ENTRADA_COMPRA', 100, 3.50, 100, 'Stock inicial de demostración');
