package com.facturadorsri.backend.service.sri.firma;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que la firma XAdES-BES generada sea criptograficamente valida
 * segun el estandar XML-DSig (W3C), de forma independiente al SRI (que no
 * se puede probar sin RUC y firma real, ver docs/investigacion.md).
 */
class XadesBesSignerTest {

    private static final String XML_DE_PRUEBA = """
            <?xml version="1.0" encoding="UTF-8"?>
            <factura id="comprobante" version="1.1.0">
                <infoTributaria>
                    <ambiente>1</ambiente>
                    <claveAcceso>2209202601179001234400110010010000000011234567816</claveAcceso>
                </infoTributaria>
            </factura>
            """;

    @Test
    void laFirmaGeneradaEsCriptograficamenteValida() throws Exception {
        CertificadoAutofirmadoProvider certificadoProvider = new CertificadoAutofirmadoProvider();
        XadesBesSigner signer = new XadesBesSigner();

        String xmlFirmado = signer.firmar(
                XML_DE_PRUEBA, certificadoProvider.obtenerClavePrivada(), certificadoProvider.obtenerCertificado());

        assertThat(xmlFirmado).contains("http://www.w3.org/2000/09/xmldsig#");
        assertThat(xmlFirmado).contains("etsi:SignedProperties");
        assertThat(xmlFirmado).contains("etsi:SigningCertificate");

        assertThat(validarFirma(xmlFirmado)).isTrue();
    }

    @Test
    void unaFirmaAlteradaNoEsValida() throws Exception {
        CertificadoAutofirmadoProvider certificadoProvider = new CertificadoAutofirmadoProvider();
        XadesBesSigner signer = new XadesBesSigner();

        String xmlFirmado = signer.firmar(
                XML_DE_PRUEBA, certificadoProvider.obtenerClavePrivada(), certificadoProvider.obtenerCertificado());

        // Se altera el contenido firmado (clave de acceso) sin volver a firmar
        String xmlAlterado = xmlFirmado.replace("2209202601179001234400110010010000000011234567816",
                "9999999999999999999999999999999999999999999999999");

        assertThat(validarFirma(xmlAlterado)).isFalse();
    }

    private boolean validarFirma(String xmlFirmado) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document documento = factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xmlFirmado.getBytes(StandardCharsets.UTF_8)));

        Element raiz = documento.getDocumentElement();
        raiz.setIdAttribute("id", true);

        NodeList nodosFirma = documento.getElementsByTagNameNS(
                "http://www.w3.org/2000/09/xmldsig#", "Signature");
        Element nodoFirma = (Element) nodosFirma.item(0);

        NodeList signedProperties = documento.getElementsByTagNameNS(
                "http://uri.etsi.org/01903/v1.3.2#", "SignedProperties");
        Element propiedadesFirmadas = (Element) signedProperties.item(0);
        propiedadesFirmadas.setIdAttribute("Id", true);

        DOMValidateContext contexto = new DOMValidateContext(
                new javax.xml.crypto.KeySelector() {
                    @Override
                    public javax.xml.crypto.KeySelectorResult select(
                            javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo,
                            Purpose purpose, javax.xml.crypto.AlgorithmMethod method,
                            javax.xml.crypto.XMLCryptoContext context) {
                        for (Object info : keyInfo.getContent()) {
                            if (info instanceof javax.xml.crypto.dsig.keyinfo.X509Data x509Data) {
                                for (Object dataObj : x509Data.getContent()) {
                                    if (dataObj instanceof java.security.cert.X509Certificate cert) {
                                        return () -> cert.getPublicKey();
                                    }
                                }
                            }
                        }
                        throw new IllegalStateException("No se encontro certificado en KeyInfo");
                    }
                }, nodoFirma);
        contexto.setIdAttributeNS(raiz, null, "id");
        contexto.setIdAttributeNS(propiedadesFirmadas, null, "Id");
        // El SRI exige RSA-SHA1 (ver docs/investigacion.md seccion 6); el modo
        // "secure validation" del JDK bloquea ese algoritmo por defecto al
        // validar, asi que se desactiva solo para esta verificacion de prueba.
        contexto.setProperty("javax.xml.crypto.dsig.secureValidation", Boolean.FALSE);
        contexto.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);

        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature signature = xmlSignatureFactory.unmarshalXMLSignature(contexto);
        return signature.validate(contexto);
    }
}
