package com.facturadorsri.backend.service.sri.soap;

import com.facturadorsri.backend.config.SriProperties;
import com.facturadorsri.backend.exception.SriIntegrationException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El modo SIMULADO del proyecto usa exactamente este mecanismo: WireMock
 * hace de doble de RecepcionComprobantesOffline y
 * AutorizacionComprobantesOffline, incluyendo rechazos ("DEVUELTA") y
 * "EN PROCESAMIENTO" (enunciado del proyecto, seccion MVP).
 */
class SriSoapClientTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    private SriSoapClient cliente;

    @BeforeEach
    void setUp() {
        SriProperties propiedades = new SriProperties("SIMULADO", "1",
                wireMock.baseUrl() + "/RecepcionComprobantesOffline",
                wireMock.baseUrl() + "/AutorizacionComprobantesOffline",
                null, null, "2.34", new SriProperties.Reintentos(5, 30));
        cliente = new SriSoapClient(propiedades);
    }

    @Test
    void unComprobanteValidoEsRecibido() {
        wireMock.stubFor(post("/RecepcionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaSolicitud>
                      <estado>RECIBIDA</estado>
                    </RespuestaSolicitud>
                  </soap:Body>
                </soap:Envelope>
                """)));

        RespuestaRecepcion respuesta = cliente.enviarRecepcion("<factura/>");

        assertThat(respuesta.fueRecibida()).isTrue();
    }

    @Test
    void unComprobanteConErroresEsDevuelto() {
        wireMock.stubFor(post("/RecepcionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaSolicitud>
                      <estado>DEVUELTA</estado>
                      <comprobantes>
                        <comprobante>
                          <mensajes>
                            <mensaje>
                              <identificador>35</identificador>
                              <mensaje>SECUENCIAL_REGISTRADO</mensaje>
                              <tipo>ERROR</tipo>
                            </mensaje>
                          </mensajes>
                        </comprobante>
                      </comprobantes>
                    </RespuestaSolicitud>
                  </soap:Body>
                </soap:Envelope>
                """)));

        RespuestaRecepcion respuesta = cliente.enviarRecepcion("<factura/>");

        assertThat(respuesta.fueRecibida()).isFalse();
        assertThat(respuesta.mensajes()).hasSize(1);
        assertThat(respuesta.mensajes().get(0).identificador()).isEqualTo("35");
    }

    @Test
    void unaClaveDeAccesoAutorizadaDevuelveNumeroYFecha() {
        wireMock.stubFor(post("/AutorizacionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaAutorizacionComprobante>
                      <autorizaciones>
                        <autorizacion>
                          <estado>AUTORIZADO</estado>
                          <numeroAutorizacion>2209202601179001234400110010010000000011234567816</numeroAutorizacion>
                          <fechaAutorizacion>2026-09-22T10:00:00-05:00</fechaAutorizacion>
                          <comprobante>&lt;factura/&gt;</comprobante>
                        </autorizacion>
                      </autorizaciones>
                    </RespuestaAutorizacionComprobante>
                  </soap:Body>
                </soap:Envelope>
                """)));

        RespuestaAutorizacion respuesta = cliente.consultarAutorizacion("2209202601179001234400110010010000000011234567816");

        assertThat(respuesta.estaAutorizado()).isTrue();
        assertThat(respuesta.numeroAutorizacion())
                .isEqualTo("2209202601179001234400110010010000000011234567816");
    }

    @Test
    void unaClaveDeAccesoEnProcesamientoSeReporataComoTal() {
        wireMock.stubFor(post("/AutorizacionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaAutorizacionComprobante>
                      <autorizaciones>
                        <autorizacion>
                          <estado>EN PROCESAMIENTO</estado>
                        </autorizacion>
                      </autorizaciones>
                    </RespuestaAutorizacionComprobante>
                  </soap:Body>
                </soap:Envelope>
                """)));

        RespuestaAutorizacion respuesta = cliente.consultarAutorizacion("clave");

        assertThat(respuesta.estaEnProcesamiento()).isTrue();
        assertThat(respuesta.estaAutorizado()).isFalse();
    }

    @Test
    void unaClaveDeAccesoNoAutorizadaIncluyeElMensajeDeError() {
        wireMock.stubFor(post("/AutorizacionComprobantesOffline").willReturn(okXml("""
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <RespuestaAutorizacionComprobante>
                      <autorizaciones>
                        <autorizacion>
                          <estado>NO AUTORIZADO</estado>
                          <mensajes>
                            <mensaje>
                              <identificador>43</identificador>
                              <mensaje>CLAVE_ACCESO_INVALIDA</mensaje>
                              <tipo>ERROR</tipo>
                            </mensaje>
                          </mensajes>
                        </autorizacion>
                      </autorizaciones>
                    </RespuestaAutorizacionComprobante>
                  </soap:Body>
                </soap:Envelope>
                """)));

        RespuestaAutorizacion respuesta = cliente.consultarAutorizacion("clave");

        assertThat(respuesta.estaAutorizado()).isFalse();
        assertThat(respuesta.mensajes().get(0).mensaje()).isEqualTo("CLAVE_ACCESO_INVALIDA");
    }

    @Test
    void siElServicioNoResponde200SeLanzaExcepcionDeIntegracion() {
        wireMock.stubFor(post("/RecepcionComprobantesOffline").willReturn(serverError()));

        assertThatThrownBy(() -> cliente.enviarRecepcion("<factura/>"))
                .isInstanceOf(SriIntegrationException.class);
    }
}
