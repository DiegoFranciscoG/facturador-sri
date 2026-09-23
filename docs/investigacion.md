# Investigación técnica — Facturación electrónica SRI (esquema offline)

Fecha de consulta de todas las fuentes: **2026-09-22 / 2026-09-23**.

## Nota metodológica importante

El dominio de descargas de la Alfresco de sri.gob.ec
(`www.sri.gob.ec/o/sri-portlet-biblioteca-alfresco-internet/descargar/...`)
está protegido por un WAF que, ante peticiones automatizadas (curl, fetch de
servidor, navegador headless sin sesión de usuario), **redirige con 302 hacia
el portal de login `srienlinea.sri.gob.ec`** en lugar de servir el PDF/ZIP.
Se confirmó esto con tres métodos distintos (curl, WebFetch, navegador
integrado) y los tres fueron redirigidos igual. La página HTML pública
`sri.gob.ec/en/facturacion-electronica` sí es accesible y desde ahí se
confirmó la versión vigente y los enlaces oficiales.

**Consecuencia práctica:** no pude leer el texto completo de la Ficha Técnica
ni los XSD directamente. Cada dato de las tablas siguientes indica si viene de
la página oficial (primaria), o si fue corroborado por triangulación entre
varias fuentes independientes (documentación de software de facturación
ecuatoriano y una librería open-source que factura contra el SRI real).
Ningún dato depende de una sola fuente no oficial sin corroborar.

**Acción pendiente para ti:** descarga tú mismo, desde el navegador ya
autenticado, estos dos recursos oficiales y colócalos en el repo antes de D2
para que los tests de XSD sean 100% confiables:
1. Ficha Técnica v2.34 (jul-2026): enlace en la tabla de fuentes abajo.
2. ZIP de XSD de Factura, Nota de Crédito, Nota de Débito, Retención y Guía
   de Remisión: enlaces en la tabla de fuentes abajo.

Si al abrirlos algún catálogo difiere de lo documentado aquí, aviso y
corrijo el modelo antes de programar.

## 1. Fuentes consultadas

| # | Fuente | URL | Fecha consulta | Tipo | Qué dato aporta |
|---|--------|-----|-----------------|------|------------------|
| 1 | SRI — Facturación electrónica (página oficial) | https://www.sri.gob.ec/en/facturacion-electronica | 2026-09-22 | Primaria (gob.ec) | Confirma versión vigente de la Ficha Técnica: **v2.34, actualizada julio 2026**. Enlaces oficiales a Ficha Técnica y ZIP de XSD por tipo de comprobante. |
| 2 | Ficha Técnica Comprobantes Electrónicos Esquema Offline v2.34 (PDF oficial) | https://www.sri.gob.ec/o/sri-portlet-biblioteca-alfresco-internet/descargar/f8d9bb36-5632-4f96-b463-b9265b55338c/FICHA%20TE%CC%81CNICA%20COMPROBANTES%20ELECTRO%CC%81NICOS%20ESQUEMA%20OFFLINE%20Versio%CC%81n%202.34.pdf | 2026-09-22 (enlace verificado, contenido no legible por WAF) | Primaria (gob.ec) | Documento maestro; descarga manual pendiente (ver nota arriba). |
| 3 | ZIP XSD Factura (v1.0.0–2.1.0) | https://www.sri.gob.ec/o/sri-portlet-biblioteca-alfresco-internet/descargar/05546998-6f29-4870-be3b-62650f312a6c/XML%20y%20XSD%20Factura.zip | 2026-09-22 (enlace verificado) | Primaria (gob.ec) | XSD oficiales de Factura. Descarga manual pendiente. |
| 4 | ZIP XSD Nota de Crédito | https://www.sri.gob.ec/o/sri-portlet-biblioteca-alfresco-internet/descargar/dfc944cd-5f18-4433-a626-3cc64cfc4549/XML%20y%20XSD%20Nota%20de%20Cr%C3%A9dito.zip | 2026-09-22 (enlace verificado) | Primaria (gob.ec) | XSD oficiales de Nota de Crédito. |
| 5 | ZIP XSD Retención | https://www.sri.gob.ec/o/sri-portlet-biblioteca-alfresco-internet/descargar/90950fca-73a7-4cfb-9c2d-3142b10435f2/XML%20y%20XSD%20Comprobante%20de%20Retenci%C3%B3n.zip | 2026-09-22 (enlace verificado) | Primaria (gob.ec) | XSD oficiales de Retención (útil para D+ del roadmap). |
| 6 | Ámbito de trámite "Autorización en ambientes de pruebas" | https://www.gob.ec/sri/tramites/autorizacion-ambientes-pruebas-comprobantes-electronicos | 2026-09-22 | Primaria (gob.ec) | Confirma que el ambiente de pruebas requiere RUC + clave SRI + firma electrónica; trámite gratuito. |
| 7 | GitHub `miguelangarano/open-factura` (código fuente TypeScript) | https://github.com/miguelangarano/open-factura | 2026-09-22 | Secundaria corroborada (librería open-source funcional contra el SRI real) | Algoritmo exacto de clave de acceso y dígito verificador módulo 11; endpoints WSDL de prueba/producción; estructura de campos del XML de factura v1.0.0; estructura de la firma XAdES-BES (namespaces, referencias, SignedProperties). |
| 8 | Repositorios `Jairbal/mora-sri-gateway`, `JoaquinZc/open-api-facturacion-sri`, `joselo/sri` | https://github.com/Jairbal/mora-sri-gateway , https://github.com/JoaquinZc/open-api-facturacion-sri , https://github.com/joselo/sri | 2026-09-22 | Secundaria (mención) | Confirman que XAdES-BES + PKCS#12 es el patrón estándar de la comunidad ecuatoriana para firmar contra el SRI; no se leyó su código en detalle. |
| 9 | Factuplan (proveedor de software de facturación EC) — varios artículos | https://factuplan.com.ec/blog/* | 2026-09-22 | Secundaria corroborada | Estructura de los 49 dígitos de la clave de acceso; catálogo de códigos de porcentaje de IVA vigente 2026 (0/2/3/4/5/6/7/8); catálogo Tabla 24 formas de pago. |
| 10 | DeLey (guías legales/tributarias EC) | https://deley.ec/guias/* | 2026-09-22 | Secundaria corroborada | Corrobora estructura de clave de acceso y catálogo de formas de pago. |
| 11 | Quipuy (proveedor de firma electrónica .p12) | https://quipuy.com/blog/clave-de-acceso-sri-49-digitos-explicado | 2026-09-22 | Secundaria corroborada | Corrobora estructura de clave de acceso dígito por dígito. |
| 12 | GitLab Mintel — Gobierno Electrónico, issue #32 | https://minka.gob.ec/mintel/ge/rutr/gobec_forms/-/issues/32 | 2026-09-22 | Primaria (entidad pública EC) | Algoritmo de validación de RUC (módulo 10 persona natural, módulo 11 sociedades), reglas del tercer dígito. |
| 13 | Ministerio del Trabajo / IESS (vía contexto ya verificado por el usuario) | — | 2026 | Primaria | SBU 2026 = USD 482; aportes IESS 9.45% trabajador / 11.15% empleador (referencia para campos de nómina si se requieren, no crítico para este proyecto). |

## 2. Esquema offline: qué implica

Fuente: #1, #6.

En el **esquema offline**, el propio emisor genera la clave de acceso (que
también actúa como número de autorización), firma el comprobante y lo entrega
al cliente de inmediato; el envío a los web services de Recepción y
Autorización del SRI ocurre después (asíncrono), y el SRI puede: autorizar,
o devolver **NO AUTORIZADO** / **DEVUELTA** según errores de validación.
Esto es justo el ciclo de estados que pide el proyecto
(`GENERADO → FIRMADO → RECIBIDO → DEVUELTA / AUTORIZADO / NO_AUTORIZADO`).

## 3. Clave de acceso (49 dígitos)

Fuentes: #6 (código verificado), #9, #10, #11 (corroboración cruzada, 3 fuentes
independientes coinciden exactamente).

| Posición | Longitud | Campo | Detalle |
|---|---|---|---|
| 1–8 | 8 | Fecha de emisión | `ddmmaaaa` |
| 9–10 | 2 | Tipo de comprobante | Tabla de tipo de comprobante (sección 4) |
| 11–23 | 13 | RUC del emisor | 13 dígitos |
| 24 | 1 | Ambiente | `1` pruebas, `2` producción |
| 25–27 | 3 | Establecimiento | 3 dígitos |
| 28–30 | 3 | Punto de emisión | 3 dígitos |
| 31–39 | 9 | Secuencial | 9 dígitos, con ceros a la izquierda |
| 40–47 | 8 | Código numérico | Aleatorio, generado por el emisor (offline) |
| 48 | 1 | Tipo de emisión | `1` = normal (único válido en esquema offline) |
| 49 | 1 | Dígito verificador | Módulo 11 sobre las posiciones 1–48 |

**Algoritmo del dígito verificador (módulo 11)**, verificado contra código
fuente funcional (fuente #6):

1. Recorrer los 48 dígitos de izquierda a derecha.
2. Multiplicar cada dígito por un coeficiente cíclico que empieza en `7` y
   decrece hasta `2` (`7,6,5,4,3,2,7,6,5,4,3,2,...`), reiniciando a `7`
   después de usar `2`.
3. Sumar todos los productos (`suma`).
4. `resultado = 11 - (suma % 11)`.
5. Si `resultado == 10` → dígito verificador = `1`.
6. Si `resultado == 11` → dígito verificador = `0`.
7. En cualquier otro caso, dígito verificador = `resultado`.

Este algoritmo se implementará con **casos de prueba fijos** (claves de
acceso conocidas generadas a mano) en `AccessKeyServiceTest`, y además con
property-based tests que regeneran el dígito para verificar idempotencia.

## 4. Catálogos oficiales (Ficha Técnica)

### 4.1 Tabla — Tipo de comprobante (`codDoc`)
Fuente: #6 (código), corroborado por #9/#10.

| Código | Comprobante |
|---|---|
| 01 | Factura |
| 03 | Liquidación de compra de bienes y prestación de servicios |
| 04 | Nota de crédito |
| 05 | Nota de débito |
| 06 | Guía de remisión |
| 07 | Comprobante de retención |

> No existe el código `02`; es salto documentado en la propia Ficha Técnica
> desde versiones antiguas (confirmado por triangulación, no por lectura
> directa del PDF — **SUPUESTO de bajo riesgo**, se revalida al abrir el PDF).

### 4.2 Tabla — Tipo de identificación (comprador / proveedor)
Fuente: #6 (código), corroborado por #9.

| Código | Tipo |
|---|---|
| 04 | RUC |
| 05 | Cédula |
| 06 | Pasaporte |
| 07 | Consumidor final (identificación = `9999999999999`, 13 nueves) |
| 08 | Identificación del exterior |

### 4.3 Tabla — Ambiente
| Código | Ambiente |
|---|---|
| 1 | Pruebas |
| 2 | Producción |

### 4.4 Tabla — Tipo de emisión
| Código | Tipo |
|---|---|
| 1 | Normal (único usado en esquema offline) |

### 4.5 Tabla — Código de impuesto
Fuente: #6 (comentario en código), corroborado por búsqueda de Tabla 18/ICE.

| Código | Impuesto |
|---|---|
| 2 | IVA |
| 3 | ICE (Impuesto a los Consumos Especiales) |
| 5 | IRBPNR (Impuesto Redimible a Botellas Plásticas No Retornables) |

### 4.6 Tabla — Código de porcentaje de IVA (`codigoPorcentaje`)
Fuente: #9 (corroborado con dos artículos independientes de 2026).
**Marcado como SUPUESTO a confirmar contra el PDF v2.34**: la asignación
exacta de `4` = 15% y `5` = 5% aparece consistente en fuentes 2025-2026, pero
como el IVA general subió de 12% a 15% en una reforma reciente, es el punto
del catálogo con más probabilidad de haber cambiado de número de versión a
versión. Verificar al abrir la Ficha Técnica v2.34.

| Código | Tarifa | Nota |
|---|---|---|
| 0 | 0% | Bienes/servicios tarifa 0 |
| 2 | 12% | Histórico |
| 3 | 14% | Histórico (vigencia 2016) |
| 4 | 15% | **Tarifa general vigente 2026** |
| 5 | 5% | Tarifa diferenciada (p. ej. vivienda de interés social) |
| 6 | — | No objeto de impuesto |
| 7 | — | Exento de IVA |
| 8 | 8% | Diferenciada (turismo en feriados declarados) |

### 4.7 Tabla 24 — Formas de pago (`formaPago`)
Fuente: PDF oficial "formas de pago.pdf" (enlace confirmado en #1/búsqueda,
contenido no legible por WAF), corroborado por #9 y #10 con lista idéntica.

| Código | Forma de pago |
|---|---|
| 01 | Sin utilización del sistema financiero |
| 15 | Compensación de deudas |
| 16 | Tarjeta de débito |
| 17 | Dinero electrónico |
| 18 | Tarjeta prepago |
| 19 | Tarjeta de crédito |
| 20 | Otros con utilización del sistema financiero |
| 21 | Endoso de títulos |

## 5. Validación de identificadores (RUC / cédula)

Fuente: #12 (Mintel, entidad pública) — algoritmo estándar usado en todo
software ecuatoriano.

- **Cédula** (10 dígitos): módulo 10, coeficientes `2,1,2,1,2,1,2,1,2` sobre
  las primeras 9 posiciones; si el producto de un dígito por 2 supera 9, se
  resta 9. El dígito verificador es la posición 10.
- **RUC persona natural** (13 dígitos): cédula válida + terminación `001`.
- **RUC sociedad pública** (tercer dígito = `6`): módulo 11, coeficientes
  `3,2,7,6,5,4,3,2` sobre las 8 primeras posiciones.
- **RUC sociedad privada / extranjero sin cédula** (tercer dígito = `9`):
  módulo 11, coeficientes `4,3,2,7,6,5,4,3,2` sobre las 9 primeras posiciones.
- Tercer dígito `0–5`: persona natural (cédula). `6`: sociedad pública.
  `9`: sociedad privada o extranjero.

## 6. Firma electrónica XAdES-BES

Fuente: #6 (implementación funcional completa leída línea por línea),
corroborado por menciones en #8.

- Formato de certificado: **PKCS#12 (.p12)**, emitido por una Entidad de
  Certificación de Información acreditada por ARCOTEL (ej. Security Data,
  Banco Central del Ecuador).
- Firma XML: **XAdES-BES** (perfil "Basic Electronic Signature" de ETSI TS
  101 903), con:
  - `SignatureMethod` = `http://www.w3.org/2000/09/xmldsig#rsa-sha1` (RSA
    con SHA-1 — exigido por el propio esquema del SRI, no es una elección
    nuestra; **es criptográficamente débil para 2026 pero obligatorio para
    interoperar con el SRI**, se documentará como riesgo aceptado en
    `SECURITY.md`).
  - `CanonicalizationMethod` = `http://www.w3.org/TR/2001/REC-xml-c14n-20010315`
    (Canonical XML 1.0).
  - `Transform` = `enveloped-signature`.
  - `DigestMethod` = SHA-1 sobre: el documento completo (`Reference URI="#comprobante"`),
    el bloque `KeyInfo` (certificado) y el bloque `SignedProperties`
    (con namespaces `ds`/`etsi` inyectados antes del hash — es una
    particularidad del SRI que hay que replicar exactamente o el SRI
    rechaza la firma).
  - `QualifyingProperties` (`etsi:`) con `SigningTime`, `SigningCertificate`
    (digest + emisor + número de serie) y `DataObjectFormat`.
- Librería recomendada para Java: **Apache Santuario (XML Security for
  Java)** para construir la firma manualmente con esos parámetros exactos
  (xades4j apunta a XAdES-EPES/T con perfiles más estrictos y puede no
  encajar 1:1 con las particularidades del SRI); se validará generando una
  firma y comparándola campo a campo contra un comprobante autorizado real
  de ejemplo.
- El certificado de pruebas del proyecto será **autofirmado, generado en
  tiempo de ejecución** (nunca versionado), únicamente para el modo
  SIMULADO; no será válido contra el SRI real.

## 7. Web services (WSDL)

Fuente: #6 (URLs usadas en producción por una librería funcional), #1.

| Ambiente | Servicio | URL |
|---|---|---|
| Pruebas | Recepción | `https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline?wsdl` |
| Pruebas | Autorización | `https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline?wsdl` |
| Producción | Recepción | `https://cel.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline?wsdl` |
| Producción | Autorización | `https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline?wsdl` |

Respuestas de Recepción: `RECIBIDA` (pasó validación de esquema/clave de
acceso) o `DEVUELTA` (rechazada, con `mensajes` de error). Respuestas de
Autorización: `AUTORIZADO`, `NO AUTORIZADO`, o `EN PROCESAMIENTO`
(hay que reintentar la consulta). Estos tres estados + `RECIBIDO` /
`FIRMADO` / `GENERADO` mapean 1:1 al enum `EstadoComprobante` pedido en el
enunciado.

## 8. RIDE (representación impresa)

Fuente: búsqueda agregada corroborada por múltiples asesorías tributarias EC,
y Resolución NAC-DGERCGC18-00000233 (jun-2018, SRI) citada por esas mismas
fuentes — **pendiente de verificar el texto exacto de la resolución**
(SUPUESTO de bajo riesgo, es una resolución muy citada y estable desde 2018).

Campos obligatorios: datos del emisor (razón social, nombre comercial, RUC,
dirección matriz y establecimiento), datos del comprador, fecha de emisión,
número de autorización, clave de acceso (49 dígitos) en texto, detalle de
la transacción, valores/impuestos desglosados, y **código de barras** que
represente la clave de acceso en formato **GS1-128** (Code 128). El RIDE
en PDF tiene la misma validez tributaria que el XML. Retención documental:
mínimo 7 años (emisor y receptor).

## 9. Riesgos / SUPUESTOS pendientes de verificar contra fuentes oficiales

Estado al cierre de D5: el codigo ya esta construido y probado contra estos
supuestos (WireMock, tests unitarios y de firma), pero ninguno de ellos se
pudo contrastar contra el documento oficial del SRI por el bloqueo del WAF
descrito en la nota metodologica al inicio de este documento.

| # | Supuesto | Riesgo si es incorrecto | Cómo se resuelve |
|---|---|---|---|
| 1 | Códigos exactos de `codigoPorcentaje` (sección 4.6) | Bajo — el 15% es el único usado en el MVP (venta general); si el código real fuera otro, es un cambio de una constante en `V6__seed_catalogos_sri.sql` | Abrir Ficha Técnica v2.34 y contrastar Anexo de tablas |
| 2 | Estructura exacta de los XSD (solo se infirió de un XML de ejemplo, no del XSD real) | Medio — un `minOccurs`/`maxOccurs` mal inferido rompe la validación XSD contra el SRI real, aunque el XML ya se probó bien formado y con los totales cuadrados (`ComprobanteXmlBuilderTest`) | Descargar el ZIP oficial de XSD (enlaces en sección 1), generar las clases JAXB con `xjc` directamente desde ellos y reemplazar `service/sri/xml/*.java` |
| 3 | Redacción exacta de la Resolución NAC-DGERCGC18-00000233 sobre RIDE | Bajo — solo afecta redacción del `SECURITY.md`/README, no la lógica | Buscar el texto en el Registro Oficial si se necesita citar textualmente |
| 4 | Si el ambiente de pruebas actual sigue en `celcer.sri.gob.ec` (no cambió de host) | Medio — si cambió, el cliente PRUEBAS-SRI opcional no conectará | Confirmar con un `nslookup`/ping manual tuyo o al activar el modo PRUEBAS-SRI por primera vez |
| 5 | Namespaces y forma exacta del sobre SOAP (`http://ec.gob.sri.ws.recepcion`/`.autorizacion`, nombres de las operaciones y de los campos de respuesta) — ver `SriSoapClient.java` | Medio — si el SRI real espera un namespace o nombre de campo distinto, el modo PRUEBAS-SRI fallaria aunque el modo SIMULADO (WireMock, que usa exactamente estos mismos nombres) siga funcionando igual | Capturar con un sniffer HTTP un envío real (o el WSDL, una vez accesible) y ajustar `SriSoapClient.enviarRecepcion`/`consultarAutorizacion` |

## 10. Datos ya verificados por ti (contexto de la cuenta, no re-verificados aquí)

- IVA general 2026: 15% (BPONE, sri.gob.ec).
- Render free, Neon free, límites de despliegue: ya documentados en tus
  reglas maestras, se usarán tal cual en la fase de despliegue (D6).

## 11. Limitaciones del entorno de desarrollo durante D2-D5

- **Docker Desktop no llegó a inicializar** en esta máquina durante la
  construcción (WSL2 arranca bien, pero el backend de Docker nunca expuso su
  socket — probablemente un diálogo de primer arranque pendiente). Por eso
  el test de integración `VentaFlujoCompletoIntegrationTest` (Testcontainers)
  no se pudo ejecutar aquí, aunque compila y está listo; correrá normalmente
  en GitHub Actions (Docker viene activo en los runners) o en tu máquina una
  vez que actives Docker Desktop.
- Por el mismo motivo no se pudo hacer una prueba manual de extremo a
  extremo (levantar el backend real + Postgres + frontend juntos y hacer
  clic en el flujo completo). Sí se verificó, cada una por separado y con
  evidencia reproducible: el algoritmo de clave de acceso (casos calculados
  a mano), los validadores de cédula/RUC, la generación y estructura del XML,
  la validez criptográfica de la firma XAdES-BES (auto-verificación con
  `javax.xml.crypto`), el cliente SOAP contra un WireMock real, la máquina de
  estados del comprobante, y el frontend (build de producción + build de
  desarrollo renderizado en el navegador).
- La paleta de colores del frontend (navy/teal) es una elección propia:
  no tuve acceso a los otros proyectos del portafolio para replicar su
  paleta exacta como pediste. Se puede ajustar en
  `frontend/src/styles.css` (bloque `@theme`) en un minuto si tienes una
  paleta de referencia.
