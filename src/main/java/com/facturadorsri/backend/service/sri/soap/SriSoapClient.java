package com.facturadorsri.backend.service.sri.soap;

import com.facturadorsri.backend.config.SriProperties;
import com.facturadorsri.backend.exception.SriIntegrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Cliente SOAP para RecepcionComprobantesOffline y
 * AutorizacionComprobantesOffline. El mismo cliente sirve para el modo
 * SIMULADO (apunta a WireMock) y PRUEBAS-SRI (apunta al ambiente de pruebas
 * real): lo unico que cambia es la URL configurada (facturador.sri.*-url).
 *
 * Los namespaces de operacion ("http://ec.gob.sri.ws.recepcion" /
 * ".autorizacion") y la forma de la respuesta SOAP son los documentados de
 * forma consistente en multiples integraciones community de terceros
 * (docs/investigacion.md fuente #6 y #8); no se pudieron verificar contra el
 * WSDL oficial (bloqueado por el mismo WAF que los PDF, ver investigacion.md).
 */
@Component
@RequiredArgsConstructor
public class SriSoapClient {

    private final SriProperties sriProperties;
    private final RestClient restClient = RestClient.create();

    public RespuestaRecepcion enviarRecepcion(String xmlFirmado) {
        String xmlBase64 = Base64.getEncoder().encodeToString(xmlFirmado.getBytes(StandardCharsets.UTF_8));
        String envelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.recepcion">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <ec:validarComprobante>
                         <xml>%s</xml>
                      </ec:validarComprobante>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(xmlBase64);

        Document respuesta = enviarSoap(sriProperties.recepcionUrl(), envelope, "validarComprobante");
        String estado = textoDe(respuesta, "estado");
        List<MensajeSri> mensajes = extraerMensajes(respuesta.getDocumentElement());
        return new RespuestaRecepcion(estado, mensajes);
    }

    public RespuestaAutorizacion consultarAutorizacion(String claveAcceso) {
        String envelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ec="http://ec.gob.sri.ws.autorizacion">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <ec:autorizacionComprobante>
                         <claveAccesoComprobante>%s</claveAccesoComprobante>
                      </ec:autorizacionComprobante>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(claveAcceso);

        Document respuesta = enviarSoap(sriProperties.autorizacionUrl(), envelope, "autorizacionComprobante");

        NodeList autorizaciones = respuesta.getElementsByTagName("autorizacion");
        if (autorizaciones.getLength() == 0) {
            throw new SriIntegrationException("La respuesta de autorizacion no contiene ninguna autorizacion");
        }
        Element autorizacion = (Element) autorizaciones.item(0);
        String estado = textoDe(autorizacion, "estado");
        String numeroAutorizacion = textoDe(autorizacion, "numeroAutorizacion");
        String fechaAutorizacion = textoDe(autorizacion, "fechaAutorizacion");
        String comprobanteXml = textoDe(autorizacion, "comprobante");
        List<MensajeSri> mensajes = extraerMensajes(autorizacion);

        return new RespuestaAutorizacion(estado, numeroAutorizacion, fechaAutorizacion, comprobanteXml, mensajes);
    }

    private Document enviarSoap(String url, String envelope, String operacion) {
        try {
            String cuerpoRespuesta = restClient.post()
                    .uri(url)
                    .header("Content-Type", "text/xml; charset=UTF-8")
                    .header("SOAPAction", operacion)
                    .body(envelope)
                    .retrieve()
                    .body(String.class);
            return parsear(cuerpoRespuesta);
        } catch (RestClientException e) {
            throw new SriIntegrationException(
                    "Fallo al comunicarse con el servicio SRI (%s): %s".formatted(url, e.getMessage()), e);
        }
    }

    private Document parsear(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            return factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new SriIntegrationException("La respuesta del SRI no es un XML valido: " + e.getMessage(), e);
        }
    }

    /**
     * "mensaje" es a la vez el contenedor (<mensajes><mensaje>...) y el
     * nombre del campo de texto dentro de ese mismo contenedor
     * (<mensaje><mensaje>SECUENCIAL_REGISTRADO</mensaje></mensaje>). Por eso
     * NO se puede usar getElementsByTagName (recursivo) para listar los
     * contenedores: hay que quedarse solo con los hijos DIRECTOS de
     * "mensajes" para no confundir el campo de texto con otro mensaje mas.
     */
    private List<MensajeSri> extraerMensajes(Element contexto) {
        List<MensajeSri> mensajes = new ArrayList<>();
        NodeList contenedores = contexto.getElementsByTagName("mensajes");
        for (int i = 0; i < contenedores.getLength(); i++) {
            for (Element mensaje : hijosDirectos((Element) contenedores.item(i), "mensaje")) {
                mensajes.add(new MensajeSri(
                        hijoDirectoTexto(mensaje, "identificador"),
                        hijoDirectoTexto(mensaje, "mensaje"),
                        hijoDirectoTexto(mensaje, "informacionAdicional"),
                        hijoDirectoTexto(mensaje, "tipo")));
            }
        }
        return mensajes;
    }

    private String textoDe(Document documento, String tag) {
        NodeList nodos = documento.getElementsByTagName(tag);
        return nodos.getLength() > 0 ? nodos.item(0).getTextContent() : null;
    }

    private String textoDe(Element elemento, String tag) {
        NodeList nodos = elemento.getElementsByTagName(tag);
        return nodos.getLength() > 0 ? nodos.item(0).getTextContent() : null;
    }

    private List<Element> hijosDirectos(Element padre, String tagHijo) {
        List<Element> resultado = new ArrayList<>();
        NodeList hijos = padre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node hijo = hijos.item(i);
            if (hijo.getNodeType() == Node.ELEMENT_NODE && hijo.getNodeName().equals(tagHijo)) {
                resultado.add((Element) hijo);
            }
        }
        return resultado;
    }

    private String hijoDirectoTexto(Element padre, String tagHijo) {
        List<Element> hijos = hijosDirectos(padre, tagHijo);
        return hijos.isEmpty() ? null : hijos.get(0).getTextContent();
    }
}
