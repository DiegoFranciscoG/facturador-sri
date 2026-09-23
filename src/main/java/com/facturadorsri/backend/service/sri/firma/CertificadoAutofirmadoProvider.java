package com.facturadorsri.backend.service.sri.firma;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Date;

/**
 * Certificado autofirmado generado en memoria al arrancar la aplicacion.
 * Solo valido para el modo SIMULADO (WireMock): el SRI real jamas aceptaria
 * una firma con un certificado que no fue emitido por una entidad de
 * certificacion acreditada por ARCOTEL. Nunca se escribe a disco ni se
 * versiona (Regla maestra seccion 3 y enunciado del proyecto).
 */
@Component
@ConditionalOnMissingBean(CertificadoP12Provider.class)
public class CertificadoAutofirmadoProvider implements CertificadoFirmaProvider {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final KeyPair parClaves;
    private final X509Certificate certificado;

    public CertificadoAutofirmadoProvider() {
        try {
            this.parClaves = generarParClaves();
            this.certificado = generarCertificado(parClaves);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el certificado autofirmado de desarrollo", e);
        }
    }

    @Override
    public java.security.PrivateKey obtenerClavePrivada() {
        return parClaves.getPrivate();
    }

    @Override
    public X509Certificate obtenerCertificado() {
        return certificado;
    }

    private KeyPair generarParClaves() throws Exception {
        KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        return generador.generateKeyPair();
    }

    private X509Certificate generarCertificado(KeyPair parClaves) throws Exception {
        X500Name sujeto = new X500Name("CN=Facturador SRI Desarrollo, O=Demo, C=EC");
        BigInteger numeroSerie = BigInteger.valueOf(System.currentTimeMillis());
        Date desde = new Date();
        Date hasta = new Date(desde.getTime() + Duration.ofDays(365).toMillis());

        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                sujeto, numeroSerie, desde, hasta, sujeto, parClaves.getPublic());

        ContentSigner firmante = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .build(parClaves.getPrivate());

        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(builder.build(firmante));
    }
}
