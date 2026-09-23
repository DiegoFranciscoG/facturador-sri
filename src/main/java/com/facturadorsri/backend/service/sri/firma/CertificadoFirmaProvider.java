package com.facturadorsri.backend.service.sri.firma;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/** Fuente del par certificado/clave privada usado para firmar XAdES-BES. */
public interface CertificadoFirmaProvider {

    PrivateKey obtenerClavePrivada();

    X509Certificate obtenerCertificado();
}
