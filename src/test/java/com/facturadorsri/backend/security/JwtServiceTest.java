package com.facturadorsri.backend.security;

import com.facturadorsri.backend.config.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService service = new JwtService(
            new JwtProperties("una-clave-de-prueba-de-al-menos-256-bits-1234567890", 30));

    @Test
    void generaUnTokenQueSePuedeValidarYContieneElRol() {
        String token = service.generarToken("admin", "ADMIN");

        var claims = service.validar(token);

        assertThat(claims).isPresent();
        assertThat(claims.get().getSubject()).isEqualTo("admin");
        assertThat(service.rolDe(claims.get())).isEqualTo("ADMIN");
    }

    @Test
    void unTokenConFirmaAlteradaNoEsValido() {
        String token = service.generarToken("admin", "ADMIN");
        String tokenAlterado = token.substring(0, token.length() - 2) + "xx";

        assertThat(service.validar(tokenAlterado)).isEmpty();
    }

    @Test
    void unTokenCompletamenteInvalidoNoEsValido() {
        assertThat(service.validar("no-es-un-jwt")).isEmpty();
    }

    @Test
    void unSecretoDemasiadoCortoImpideGenerarTokens() {
        JwtService servicioInseguro = new JwtService(new JwtProperties("corto", 30));

        assertThatThrownBy(() -> servicioInseguro.generarToken("admin", "ADMIN"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expirationMinutesDevuelveElConfigurado() {
        assertThat(service.expirationMinutes()).isEqualTo(30);
    }
}
