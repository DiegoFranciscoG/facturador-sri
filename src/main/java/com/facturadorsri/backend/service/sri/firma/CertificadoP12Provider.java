package com.facturadorsri.backend.service.sri.firma;

import com.facturadorsri.backend.config.SriProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Certificado real (.p12) para el modo PRUEBAS-SRI, activado solo con
 * SRI_MODO=PRUEBAS-SRI y una ruta de archivo fuera del repositorio
 * (SRI_CERTIFICADO_P12_PATH). Nunca se versiona el .p12 ni su contrasena.
 */
@Component
@ConditionalOnProperty(prefix = "facturador.sri", name = "modo", havingValue = "PRUEBAS-SRI")
@RequiredArgsConstructor
public class CertificadoP12Provider implements CertificadoFirmaProvider {

    private final SriProperties sriProperties;

    private PrivateKey clavePrivada;
    private X509Certificate certificado;

    @Override
    public synchronized PrivateKey obtenerClavePrivada() {
        cargarSiNecesario();
        return clavePrivada;
    }

    @Override
    public synchronized X509Certificate obtenerCertificado() {
        cargarSiNecesario();
        return certificado;
    }

    private void cargarSiNecesario() {
        if (clavePrivada != null) {
            return;
        }
        String ruta = sriProperties.certificadoP12Path();
        String password = sriProperties.certificadoP12Password();
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalStateException(
                    "SRI_MODO=PRUEBAS-SRI requiere SRI_CERTIFICADO_P12_PATH y SRI_CERTIFICADO_P12_PASSWORD");
        }
        try (FileInputStream entrada = new FileInputStream(ruta)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(entrada, password.toCharArray());

            Enumeration<String> alias = keyStore.aliases();
            String primerAlias = alias.hasMoreElements() ? alias.nextElement() : null;
            if (primerAlias == null) {
                throw new IllegalStateException("El archivo .p12 no contiene ningun certificado");
            }
            clavePrivada = (PrivateKey) keyStore.getKey(primerAlias, password.toCharArray());
            certificado = (X509Certificate) keyStore.getCertificate(primerAlias);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar el certificado .p12: " + e.getMessage(), e);
        }
    }
}
