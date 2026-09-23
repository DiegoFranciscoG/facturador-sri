package com.facturadorsri.backend.security;

import com.facturadorsri.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Emision y validacion de JWT (HS256). El secreto viene solo de
 * facturador.jwt.secret (env JWT_SECRET); la app no arranca sin el
 * (Regla maestra seccion 3: sin valores por defecto para secretos).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_ROL = "rol";

    private final JwtProperties jwtProperties;

    private SecretKey clave() {
        byte[] bytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 256 bits (32 caracteres UTF-8)");
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generarToken(String username, String rol) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(jwtProperties.expirationMinutes() * 60L);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROL, rol)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(expiracion))
                .signWith(clave())
                .compact();
    }

    public Optional<Claims> validar(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public String rolDe(Claims claims) {
        return claims.get(CLAIM_ROL, String.class);
    }

    public long expirationMinutes() {
        return jwtProperties.expirationMinutes();
    }
}
