package com.facturadorsri.backend.integration;

import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.dto.comprobante.ComprobanteResponse;
import com.facturadorsri.backend.dto.comprobante.PagoRequest;
import com.facturadorsri.backend.dto.comprobante.VentaLineaRequest;
import com.facturadorsri.backend.dto.comprobante.VentaRequest;
import com.facturadorsri.backend.repository.MovimientoInventarioRepository;
import com.facturadorsri.backend.service.VentaService;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion de extremo a extremo: venta POS -> factura ->
 * firmado -> recibido -> autorizado, contra un Postgres real
 * (Testcontainers, migra con Flyway igual que en produccion) y un SRI
 * simulado con WireMock (modo SIMULADO, tal como pide el enunciado del
 * proyecto). Usa el emisor/establecimiento/cliente/producto/bodega
 * sembrados en V7__seed_datos_demo.sql.
 *
 * NOTA: no se pudo ejecutar en este entorno de desarrollo porque el motor
 * de Docker Desktop no llego a inicializarse (WSL2 arranca, pero el backend
 * de Docker nunca expone el socket, probablemente por un dialogo de
 * primer arranque pendiente). El codigo esta listo para correr con
 * `mvn test` en cualquier maquina con Docker activo, o en GitHub Actions
 * (donde el runner trae Docker ya inicializado).
 */
@SpringBootTest
@Testcontainers
class VentaFlujoCompletoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("facturador")
            .withUsername("facturador")
            .withPassword("facturador");

    static WireMockServer wireMockServer;

    @BeforeAll
    static void iniciarWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void detenerWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USER", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("JWT_SECRET", () -> "una-clave-de-prueba-de-al-menos-256-bits-1234567890");
        registry.add("CORS_ALLOWED_ORIGINS", () -> "http://localhost:4200");
        registry.add("ADMIN_USERNAME", () -> "admin");
        registry.add("ADMIN_PASSWORD", () -> "ClaveDePruebaSoloParaTests123!");
        registry.add("SRI_RECEPCION_URL", () -> wireMockServer.baseUrl() + "/RecepcionComprobantesOffline");
        registry.add("SRI_AUTORIZACION_URL", () -> wireMockServer.baseUrl() + "/AutorizacionComprobantesOffline");
    }

    @Autowired
    private VentaService ventaService;
    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Test
    void unaVentaCompletaLlegaAAutorizadaYRegistraElKardex() {
        wireMockServer.stubFor(post("/RecepcionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><RespuestaSolicitud><estado>RECIBIDA</estado></RespuestaSolicitud></soap:Body>
                </soap:Envelope>
                """)));
        wireMockServer.stubFor(post("/AutorizacionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaAutorizacionComprobante>
                      <autorizaciones>
                        <autorizacion>
                          <estado>AUTORIZADO</estado>
                          <numeroAutorizacion>numero-de-prueba</numeroAutorizacion>
                          <fechaAutorizacion>2026-09-22T10:00:00-05:00</fechaAutorizacion>
                          <comprobante>&lt;factura/&gt;</comprobante>
                        </autorizacion>
                      </autorizaciones>
                    </RespuestaAutorizacionComprobante>
                  </soap:Body>
                </soap:Envelope>
                """)));

        VentaRequest request = new VentaRequest(1L, 1L, 1L, 1L,
                List.of(new VentaLineaRequest(1L, new BigDecimal("2"), BigDecimal.ZERO)),
                List.of(new PagoRequest("01", new BigDecimal("23.00"), null, null)));

        ComprobanteResponse respuesta = ventaService.crearFactura(request);

        assertThat(respuesta.estado()).isEqualTo(EstadoComprobante.AUTORIZADO);
        assertThat(respuesta.claveAcceso()).hasSize(49);
        assertThat(respuesta.numeroAutorizacion()).isEqualTo("numero-de-prueba");

        assertThat(movimientoInventarioRepository.findByProductoIdAndBodegaIdOrderByFechaDesc(1L, 1L))
                .isNotEmpty();
    }

    @Test
    void unaClaveDeAccesoNuncaSeDuplicaEntreDosVentasSeguidas() {
        wireMockServer.stubFor(post("/RecepcionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body><RespuestaSolicitud><estado>RECIBIDA</estado></RespuestaSolicitud></soap:Body>
                </soap:Envelope>
                """)));
        wireMockServer.stubFor(post("/AutorizacionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaAutorizacionComprobante>
                      <autorizaciones><autorizacion><estado>EN PROCESAMIENTO</estado></autorizacion></autorizaciones>
                    </RespuestaAutorizacionComprobante>
                  </soap:Body>
                </soap:Envelope>
                """)));

        VentaRequest request = new VentaRequest(1L, 1L, 1L, 1L,
                List.of(new VentaLineaRequest(2L, new BigDecimal("1"), BigDecimal.ZERO)),
                List.of(new PagoRequest("01", new BigDecimal("5.50"), null, null)));

        ComprobanteResponse primera = ventaService.crearFactura(request);
        ComprobanteResponse segunda = ventaService.crearFactura(request);

        assertThat(primera.claveAcceso()).isNotEqualTo(segunda.claveAcceso());
        assertThat(primera.secuencial()).isNotEqualTo(segunda.secuencial());
    }
}
