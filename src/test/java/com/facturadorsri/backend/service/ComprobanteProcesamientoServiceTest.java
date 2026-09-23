package com.facturadorsri.backend.service;

import com.facturadorsri.backend.config.SriProperties;
import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.Establecimiento;
import com.facturadorsri.backend.domain.Emisor;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.repository.ComprobanteRepository;
import com.facturadorsri.backend.repository.EventoSriRepository;
import com.facturadorsri.backend.service.sri.ComprobanteXmlBuilder;
import com.facturadorsri.backend.service.sri.firma.CertificadoFirmaProvider;
import com.facturadorsri.backend.service.sri.firma.XadesBesSigner;
import com.facturadorsri.backend.service.sri.soap.MensajeSri;
import com.facturadorsri.backend.service.sri.soap.RespuestaAutorizacion;
import com.facturadorsri.backend.service.sri.soap.RespuestaRecepcion;
import com.facturadorsri.backend.service.sri.soap.SriSoapClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprobanteProcesamientoServiceTest {

    @Mock
    private ComprobanteRepository comprobanteRepository;
    @Mock
    private EventoSriRepository eventoSriRepository;
    @Mock
    private ComprobanteXmlBuilder xmlBuilder;
    @Mock
    private XadesBesSigner signer;
    @Mock
    private CertificadoFirmaProvider certificadoFirmaProvider;
    @Mock
    private SriSoapClient sriSoapClient;

    private SriProperties sriProperties;

    private ComprobanteProcesamientoService service;

    private Comprobante comprobante;

    @BeforeEach
    void setUp() {
        sriProperties = new SriProperties("SIMULADO", "1", "http://x", "http://y", null, null, "2.34",
                new SriProperties.Reintentos(5, 30));
        service = new ComprobanteProcesamientoService(comprobanteRepository, eventoSriRepository, xmlBuilder,
                signer, certificadoFirmaProvider, sriSoapClient, sriProperties);

        Emisor emisor = Emisor.builder().ruc("1790012344001").razonSocial("DEMO").direccionMatriz("x").build();
        Establecimiento establecimiento = Establecimiento.builder().emisor(emisor).codigo("001").build();
        comprobante = Comprobante.builder()
                .id(1L)
                .claveAcceso("2209202601179001234400110010010000000011234567816")
                .tipoComprobante("01")
                .establecimiento(establecimiento)
                .estado(EstadoComprobante.GENERADO)
                .intentosEnvio(0)
                .build();

        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
    }

    /** Solo la usan los tests que arrancan en GENERADO y por lo tanto pasan por firmar(). */
    private void stubFirma() {
        when(xmlBuilder.construirFacturaXml(any())).thenReturn("<factura/>");
        when(certificadoFirmaProvider.obtenerClavePrivada()).thenReturn(mock(PrivateKey.class));
        when(certificadoFirmaProvider.obtenerCertificado()).thenReturn(mock(X509Certificate.class));
        when(signer.firmar(anyString(), any(PrivateKey.class), any(X509Certificate.class)))
                .thenReturn("<factura><Signature/></factura>");
    }

    @Test
    void unComprobanteRecienGeneradoAvanzaHastaAutorizadoEnUnaSolaPasada() {
        stubFirma();
        when(sriSoapClient.enviarRecepcion(anyString()))
                .thenReturn(new RespuestaRecepcion("RECIBIDA", List.of()));
        when(sriSoapClient.consultarAutorizacion(anyString()))
                .thenReturn(new RespuestaAutorizacion("AUTORIZADO", "999", "2026-09-22", "<factura/>", List.of()));

        service.procesar(1L);

        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.AUTORIZADO);
        assertThat(comprobante.getNumeroAutorizacion()).isEqualTo("999");
        assertThat(comprobante.getXmlFirmado()).isNotNull();
        verify(comprobanteRepository).save(comprobante);
    }

    @Test
    void siElSriDevuelveElComprobanteQuedaEnDevueltaYProgramaReintento() {
        stubFirma();
        when(sriSoapClient.enviarRecepcion(anyString())).thenReturn(new RespuestaRecepcion("DEVUELTA",
                List.of(new MensajeSri("45", "SECUENCIAL_REGISTRADO", null, "ERROR"))));

        service.procesar(1L);

        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.DEVUELTA);
        assertThat(comprobante.getProximoReintentoEn()).isNotNull();
        assertThat(comprobante.getIntentosEnvio()).isEqualTo(1);
        verify(sriSoapClient, never()).consultarAutorizacion(anyString());
    }

    @Test
    void siElSriRespondeEnProcesamientoElComprobanteSeQuedaEnRecibido() {
        comprobante.setEstado(EstadoComprobante.RECIBIDO);
        comprobante.setXmlFirmado("<factura><Signature/></factura>");
        when(sriSoapClient.consultarAutorizacion(anyString()))
                .thenReturn(new RespuestaAutorizacion("EN PROCESAMIENTO", null, null, null, List.of()));

        service.procesar(1L);

        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.RECIBIDO);
        assertThat(comprobante.getProximoReintentoEn()).isNotNull();
        verify(xmlBuilder, never()).construirFacturaXml(any());
        verify(sriSoapClient, never()).enviarRecepcion(anyString());
    }

    @Test
    void unaRespuestaNoAutorizadaDejaElComprobanteEnNoAutorizadoSinReintento() {
        comprobante.setEstado(EstadoComprobante.RECIBIDO);
        comprobante.setXmlFirmado("<factura><Signature/></factura>");
        when(sriSoapClient.consultarAutorizacion(anyString()))
                .thenReturn(new RespuestaAutorizacion("NO AUTORIZADO", null, null, null,
                        List.of(new MensajeSri("43", "CLAVE_ACCESO_INVALIDA", null, "ERROR"))));

        service.procesar(1L);

        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.NO_AUTORIZADO);
        assertThat(comprobante.getProximoReintentoEn()).isNull();
    }

    @Test
    void unaFallaDeRedAlEnviarRecepcionProgramaReintentoSinCambiarElEstadoAAutorizado() {
        stubFirma();
        when(sriSoapClient.enviarRecepcion(anyString())).thenThrow(new RuntimeException("timeout"));

        service.procesar(1L);

        assertThat(comprobante.getEstado()).isEqualTo(EstadoComprobante.FIRMADO);
        assertThat(comprobante.getProximoReintentoEn()).isNotNull();
        assertThat(comprobante.getIntentosEnvio()).isEqualTo(1);
    }
}
