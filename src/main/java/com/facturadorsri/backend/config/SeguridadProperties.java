package com.facturadorsri.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "facturador.seguridad")
public record SeguridadProperties(
        String adminUsername,
        String adminPassword,
        int loginMaxIntentos,
        int loginBloqueoMinutos
) {
}
