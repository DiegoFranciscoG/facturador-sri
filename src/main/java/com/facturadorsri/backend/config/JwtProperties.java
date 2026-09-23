package com.facturadorsri.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "facturador.jwt")
public record JwtProperties(String secret, int expirationMinutes) {
}
