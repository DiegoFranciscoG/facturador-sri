package com.facturadorsri.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "facturador.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
