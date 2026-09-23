-- Catálogos oficiales de la Ficha Técnica de Comprobantes Electrónicos
-- versión 2.34 (julio 2026). Ver docs/investigacion.md seccion 4 para las
-- fuentes de cada valor; los marcados como SUPUESTO ahí deben confirmarse
-- contra el PDF oficial antes de pasar a producción.

INSERT INTO catalogo_sri (catalogo, codigo, descripcion, valor_numerico, version_ficha_tecnica, vigente_desde) VALUES
-- Tabla: tipo de comprobante
('TIPO_COMPROBANTE', '01', 'Factura', NULL, '2.34', '2026-07-01'),
('TIPO_COMPROBANTE', '03', 'Liquidación de compra de bienes y prestación de servicios', NULL, '2.34', '2026-07-01'),
('TIPO_COMPROBANTE', '04', 'Nota de crédito', NULL, '2.34', '2026-07-01'),
('TIPO_COMPROBANTE', '05', 'Nota de débito', NULL, '2.34', '2026-07-01'),
('TIPO_COMPROBANTE', '06', 'Guía de remisión', NULL, '2.34', '2026-07-01'),
('TIPO_COMPROBANTE', '07', 'Comprobante de retención', NULL, '2.34', '2026-07-01'),

-- Tabla: tipo de identificación
('TIPO_IDENTIFICACION', '04', 'RUC', NULL, '2.34', '2026-07-01'),
('TIPO_IDENTIFICACION', '05', 'Cédula', NULL, '2.34', '2026-07-01'),
('TIPO_IDENTIFICACION', '06', 'Pasaporte', NULL, '2.34', '2026-07-01'),
('TIPO_IDENTIFICACION', '07', 'Venta a consumidor final', NULL, '2.34', '2026-07-01'),
('TIPO_IDENTIFICACION', '08', 'Identificación del exterior', NULL, '2.34', '2026-07-01'),

-- Tabla: ambiente
('AMBIENTE', '1', 'Pruebas', NULL, '2.34', '2026-07-01'),
('AMBIENTE', '2', 'Producción', NULL, '2.34', '2026-07-01'),

-- Tabla: tipo de emisión
('TIPO_EMISION', '1', 'Normal', NULL, '2.34', '2026-07-01'),

-- Tabla: código de impuesto
('CODIGO_IMPUESTO', '2', 'IVA', NULL, '2.34', '2026-07-01'),
('CODIGO_IMPUESTO', '3', 'ICE', NULL, '2.34', '2026-07-01'),
('CODIGO_IMPUESTO', '5', 'IRBPNR', NULL, '2.34', '2026-07-01'),

-- Tabla: código de porcentaje de IVA
('CODIGO_PORCENTAJE_IVA', '0', 'Tarifa 0%', 0.00, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '2', 'Tarifa 12% (histórico)', 12.00, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '3', 'Tarifa 14% (histórico)', 14.00, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '4', 'Tarifa 15% (general vigente)', 15.00, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '5', 'Tarifa 5% (diferenciada)', 5.00, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '6', 'No objeto de impuesto', NULL, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '7', 'Exento de IVA', NULL, '2.34', '2026-07-01'),
('CODIGO_PORCENTAJE_IVA', '8', 'Tarifa 8% (diferenciada, turismo)', 8.00, '2.34', '2026-07-01'),

-- Tabla 24: formas de pago
('FORMA_PAGO', '01', 'Sin utilización del sistema financiero', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '15', 'Compensación de deudas', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '16', 'Tarjeta de débito', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '17', 'Dinero electrónico', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '18', 'Tarjeta prepago', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '19', 'Tarjeta de crédito', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '20', 'Otros con utilización del sistema financiero', NULL, '2.34', '2026-07-01'),
('FORMA_PAGO', '21', 'Endoso de títulos', NULL, '2.34', '2026-07-01');
