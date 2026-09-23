package com.facturadorsri.backend.service.sri.firma;

import com.facturadorsri.backend.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Firma XAdES-BES (ETSI TS 101 903) sobre el XML de un comprobante,
 * replicando la estructura verificada en docs/investigacion.md seccion 6:
 * RSA-SHA1, SHA-1, Canonical XML 1.0, transform enveloped-signature, y un
 * bloque etsi:SignedProperties con SigningTime + SigningCertificate.
 *
 * Implementado con javax.xml.crypto.dsig (JSR 105), incluido en el JDK
 * (modulo java.xml.crypto) desde Java 6: no requiere Apache Santuario como
 * dependencia externa.
 */
@Component
public class XadesBesSigner {

    private static final String NS_XADES = "http://uri.etsi.org/01903/v1.3.2#";
    private static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";
    private static final String TYPE_SIGNED_PROPERTIES = "http://uri.etsi.org/01903#SignedProperties";

    public String firmar(String xmlSinFirmar, PrivateKey clavePrivada, X509Certificate certificado) { // gitleaks:allow
        try {
            Document documento = parsear(xmlSinFirmar);
            Element raiz = documento.getDocumentElement();
            marcarComoId(raiz, "id");

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");
            String idFirma = "Signature-" + UUID.randomUUID();
            String idCertificado = "Certificate-" + UUID.randomUUID();
            String idPropiedadesFirmadas = idFirma + "-SignedProperties";
            String idReferenciaDocumento = "Reference-ID-" + UUID.randomUUID();

            Element propiedadesFirmadas = construirSignedProperties(
                    documento, certificado, idPropiedadesFirmadas, idReferenciaDocumento);
            marcarComoId(propiedadesFirmadas, "Id");

            XMLObject objeto = factory.newXMLObject(
                    List.of(new DOMStructure(propiedadesFirmadas)), null, null, null);

            Reference referenciaPropiedades = factory.newReference(
                    "#" + idPropiedadesFirmadas, factory.newDigestMethod(DigestMethod.SHA1, null),
                    null, TYPE_SIGNED_PROPERTIES, null);

            Reference referenciaCertificado = factory.newReference(
                    "#" + idCertificado, factory.newDigestMethod(DigestMethod.SHA1, null));

            Reference referenciaDocumento = factory.newReference(
                    "#comprobante", factory.newDigestMethod(DigestMethod.SHA1, null),
                    List.of(factory.newTransform(Transform.ENVELOPED, (javax.xml.crypto.dsig.spec.TransformParameterSpec) null)),
                    null, idReferenciaDocumento);

            SignedInfo signedInfo = factory.newSignedInfo(
                    factory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    List.of(referenciaPropiedades, referenciaCertificado, referenciaDocumento));

            KeyInfoFactory keyInfoFactory = factory.getKeyInfoFactory();
            X509Data x509Data = keyInfoFactory.newX509Data(List.of(certificado));
            KeyValue keyValue = keyInfoFactory.newKeyValue(certificado.getPublicKey());
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(List.of(x509Data, keyValue), idCertificado);

            XMLSignature signature = factory.newXMLSignature(
                    signedInfo, keyInfo, List.of(objeto), idFirma, idFirma + "-SignatureValue");

            DOMSignContext contexto = new DOMSignContext(clavePrivada, raiz);
            contexto.setIdAttributeNS(raiz, null, "id");
            contexto.setIdAttributeNS(propiedadesFirmadas, null, "Id");

            signature.sign(contexto);

            return serializar(documento);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("No se pudo firmar el comprobante (XAdES-BES): " + e.getMessage(), e);
        }
    }

    private Element construirSignedProperties(Document documento, X509Certificate certificado,
                                               String idPropiedades, String idReferenciaDocumento) throws Exception {
        Element signedProperties = documento.createElementNS(NS_XADES, "etsi:SignedProperties");
        signedProperties.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:etsi", NS_XADES);
        signedProperties.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", NS_DS);
        signedProperties.setAttribute("Id", idPropiedades);

        Element signedSignatureProperties = documento.createElementNS(NS_XADES, "etsi:SignedSignatureProperties");
        signedProperties.appendChild(signedSignatureProperties);

        Element signingTime = documento.createElementNS(NS_XADES, "etsi:SigningTime");
        signingTime.setTextContent(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                java.time.LocalDateTime.now(ZoneOffset.UTC)));
        signedSignatureProperties.appendChild(signingTime);

        Element signingCertificate = documento.createElementNS(NS_XADES, "etsi:SigningCertificate");
        signedSignatureProperties.appendChild(signingCertificate);

        Element cert = documento.createElementNS(NS_XADES, "etsi:Cert");
        signingCertificate.appendChild(cert);

        Element certDigest = documento.createElementNS(NS_XADES, "etsi:CertDigest");
        cert.appendChild(certDigest);

        Element digestMethod = documento.createElementNS(NS_DS, "ds:DigestMethod");
        digestMethod.setAttribute("Algorithm", DigestMethod.SHA1);
        certDigest.appendChild(digestMethod);

        Element digestValue = documento.createElementNS(NS_DS, "ds:DigestValue");
        digestValue.setTextContent(sha1Base64(certificado.getEncoded()));
        certDigest.appendChild(digestValue);

        Element issuerSerial = documento.createElementNS(NS_XADES, "etsi:IssuerSerial");
        cert.appendChild(issuerSerial);

        Element issuerName = documento.createElementNS(NS_DS, "ds:X509IssuerName");
        issuerName.setTextContent(certificado.getIssuerX500Principal().getName());
        issuerSerial.appendChild(issuerName);

        Element serialNumber = documento.createElementNS(NS_DS, "ds:X509SerialNumber");
        serialNumber.setTextContent(certificado.getSerialNumber().toString());
        issuerSerial.appendChild(serialNumber);

        Element signedDataObjectProperties = documento.createElementNS(NS_XADES, "etsi:SignedDataObjectProperties");
        signedProperties.appendChild(signedDataObjectProperties);

        Element dataObjectFormat = documento.createElementNS(NS_XADES, "etsi:DataObjectFormat");
        dataObjectFormat.setAttribute("ObjectReference", "#" + idReferenciaDocumento);
        signedDataObjectProperties.appendChild(dataObjectFormat);

        Element description = documento.createElementNS(NS_XADES, "etsi:Description");
        description.setTextContent("contenido comprobante");
        dataObjectFormat.appendChild(description);

        Element mimeType = documento.createElementNS(NS_XADES, "etsi:MimeType");
        mimeType.setTextContent("text/xml");
        dataObjectFormat.appendChild(mimeType);

        return signedProperties;
    }

    private void marcarComoId(Element elemento, String nombreAtributo) {
        if (elemento.hasAttribute(nombreAtributo)) {
            elemento.setIdAttribute(nombreAtributo, true);
        }
    }

    private String sha1Base64(byte[] datos) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        return Base64.getEncoder().encodeToString(sha1.digest(datos));
    }

    private Document parsear(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String serializar(Document documento) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(documento), new StreamResult(writer));
        return writer.toString();
    }
}
