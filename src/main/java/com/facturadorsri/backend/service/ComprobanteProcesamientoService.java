package com.facturadorsri.backend.service;

import com.facturadorsri.backend.config.SriProperties;
import com.facturadorsri.backend.domain.Comprobante;
import com.facturadorsri.backend.domain.EventoSri;
import com.facturadorsri.backend.domain.enums.EstadoComprobante;
import com.facturadorsri.backend.domain.enums.TipoComprobante;
import com.facturadorsri.backend.domain.enums.TipoEventoSri;
import com.facturadorsri.backend.repository.ComprobanteRepository;
import com.facturadorsri.backend.repository.EventoSriRepository;
import com.facturadorsri.backend.service.sri.ComprobanteXmlBuilder;
import com.facturadorsri.backend.service.sri.firma.CertificadoFirmaProvider;
import com.facturadorsri.backend.service.sri.firma.XadesBesSigner;
import com.facturadorsri.backend.service.sri.soap.RespuestaAutorizacion;
import com.facturadorsri.backend.service.sri.soap.RespuestaRecepcion;
import com.facturadorsri.backend.service.sri.soap.SriSoapClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Motor de estados del comprobante: GENERADO -> FIRMADO -> RECIBIDO ->
 * (DEVUELTA | AUTORIZADO | NO_AUTORIZADO). Cada llamada a procesar() intenta
 * avanzar todo lo posible en una sola pasada; si el SRI responde
 * "EN PROCESAMIENTO" el comprobante se queda en RECIBIDO con un
 * proximo_reintento_en para que el scheduler (ReintentoSchedulerService)
 * lo retome. Ver docs/investigacion.md secciones 2 y 7.
 */
@Service
@RequiredArgsConstructor
public class ComprobanteProcesamientoService {

    private static final Logger log = LoggerFactory.getLogger(ComprobanteProcesamientoService.class);

    private final ComprobanteRepository comprobanteRepository;
    private final EventoSriRepository eventoSriRepository;
    private final ComprobanteXmlBuilder xmlBuilder;
    private final XadesBesSigner signer;
    private final CertificadoFirmaProvider certificadoFirmaProvider;
    private final SriSoapClient sriSoapClient;
    private final SriProperties sriProperties;

    /**
     * Siempre en una transaccion NUEVA: la creacion del comprobante (con el
     * bloqueo pesimista del secuencial) ya debe estar comprometida (commit)
     * antes de hablar por red con el SRI, para no retener el lock durante
     * una llamada SOAP potencialmente lenta.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void procesar(Long comprobanteId) {
        Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado: " + comprobanteId));

        if (comprobante.getEstado() == EstadoComprobante.GENERADO) {
            firmar(comprobante);
        }
        if (comprobante.getEstado() == EstadoComprobante.FIRMADO
                || comprobante.getEstado() == EstadoComprobante.DEVUELTA) {
            enviarRecepcion(comprobante);
        }
        if (comprobante.getEstado() == EstadoComprobante.RECIBIDO) {
            consultarAutorizacion(comprobante);
        }
        comprobanteRepository.save(comprobante);
    }

    private void firmar(Comprobante comprobante) {
        String xmlSinFirmar = TipoComprobante.FACTURA.getCodigo().equals(comprobante.getTipoComprobante())
                ? xmlBuilder.construirFacturaXml(comprobante)
                : xmlBuilder.construirNotaCreditoXml(comprobante);

        String xmlFirmado = signer.firmar(xmlSinFirmar,
                certificadoFirmaProvider.obtenerClavePrivada(), certificadoFirmaProvider.obtenerCertificado());

        comprobante.setXmlFirmado(xmlFirmado);
        comprobante.setEstado(EstadoComprobante.FIRMADO);
    }

    private void enviarRecepcion(Comprobante comprobante) {
        registrarEvento(comprobante, TipoEventoSri.ENVIO_RECEPCION, comprobante.getXmlFirmado(), null, null);
        try {
            RespuestaRecepcion respuesta = sriSoapClient.enviarRecepcion(comprobante.getXmlFirmado());
            registrarEvento(comprobante, TipoEventoSri.RESPUESTA_RECEPCION, null, respuesta.estado(), null);

            if (respuesta.fueRecibida()) {
                comprobante.setEstado(EstadoComprobante.RECIBIDO);
            } else {
                comprobante.setEstado(EstadoComprobante.DEVUELTA);
                programarReintento(comprobante);
            }
        } catch (Exception e) {
            log.warn("Fallo enviando recepcion del comprobante {}: {}", comprobante.getClaveAcceso(), e.getMessage());
            registrarEvento(comprobante, TipoEventoSri.RESPUESTA_RECEPCION, null, null, e.getMessage());
            programarReintento(comprobante);
        }
    }

    private void consultarAutorizacion(Comprobante comprobante) {
        registrarEvento(comprobante, TipoEventoSri.ENVIO_AUTORIZACION, comprobante.getClaveAcceso(), null, null);
        try {
            RespuestaAutorizacion respuesta = sriSoapClient.consultarAutorizacion(comprobante.getClaveAcceso());
            registrarEvento(comprobante, TipoEventoSri.RESPUESTA_AUTORIZACION, null, respuesta.estado(), null);

            if (respuesta.estaAutorizado()) {
                comprobante.setEstado(EstadoComprobante.AUTORIZADO);
                comprobante.setNumeroAutorizacion(respuesta.numeroAutorizacion());
                comprobante.setFechaAutorizacion(LocalDateTime.now());
                comprobante.setXmlAutorizacion(respuesta.comprobanteAutorizadoXml());
            } else if (respuesta.estaEnProcesamiento()) {
                programarReintento(comprobante);
            } else {
                comprobante.setEstado(EstadoComprobante.NO_AUTORIZADO);
            }
        } catch (Exception e) {
            log.warn("Fallo consultando autorizacion del comprobante {}: {}",
                    comprobante.getClaveAcceso(), e.getMessage());
            registrarEvento(comprobante, TipoEventoSri.RESPUESTA_AUTORIZACION, null, null, e.getMessage());
            programarReintento(comprobante);
        }
    }

    private void programarReintento(Comprobante comprobante) {
        int intentos = comprobante.getIntentosEnvio() + 1;
        comprobante.setIntentosEnvio(intentos);
        if (intentos > sriProperties.reintentos().maxIntentos()) {
            log.error("Comprobante {} agoto los reintentos de envio al SRI", comprobante.getClaveAcceso());
            return;
        }
        long segundos = sriProperties.reintentos().backoffBaseSegundos() * (1L << Math.min(intentos, 6));
        comprobante.setProximoReintentoEn(LocalDateTime.now().plusSeconds(segundos));
    }

    private void registrarEvento(Comprobante comprobante, TipoEventoSri tipo, String request, String estado, String error) {
        eventoSriRepository.save(EventoSri.builder()
                .comprobante(comprobante)
                .tipoEvento(tipo)
                .requestPayload(request)
                .estadoResultante(estado)
                .mensajeError(error)
                .intentoNumero(comprobante.getIntentosEnvio() + 1)
                .build());
    }
}
