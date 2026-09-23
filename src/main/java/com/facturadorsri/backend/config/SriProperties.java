package com.facturadorsri.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "facturador.sri")
public record SriProperties(
        String modo,
        String ambiente,
        String recepcionUrl,
        String autorizacionUrl,
        String certificadoP12Path,
        String certificadoP12Password,
        String versionFichaTecnica,
        Reintentos reintentos
) {
    public boolean esModoPruebasSri() {
        return "PRUEBAS-SRI".equalsIgnoreCase(modo);
    }

    public record Reintentos(int maxIntentos, int backoffBaseSegundos) {
    }
}
