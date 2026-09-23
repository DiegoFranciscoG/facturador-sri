package com.facturadorsri.backend.security;

import com.facturadorsri.backend.config.SeguridadProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterServiceTest {

    private final SeguridadProperties propiedades = new SeguridadProperties("admin", "x", 3, 15);
    private final LoginRateLimiterService service = new LoginRateLimiterService(propiedades);

    @Test
    void noEstaBloqueadoSinIntentosPrevios() {
        assertThat(service.estaBloqueado("admin")).isFalse();
    }

    @Test
    void seBloqueaAlAlcanzarElMaximoDeIntentos() {
        service.registrarFallo("admin");
        service.registrarFallo("admin");
        assertThat(service.estaBloqueado("admin")).isFalse();

        service.registrarFallo("admin");
        assertThat(service.estaBloqueado("admin")).isTrue();
    }

    @Test
    void unLoginExitosoReiniciaElContador() {
        service.registrarFallo("admin");
        service.registrarFallo("admin");
        service.registrarFallo("admin");
        assertThat(service.estaBloqueado("admin")).isTrue();

        service.registrarExito("admin");

        assertThat(service.estaBloqueado("admin")).isFalse();
    }

    @Test
    void elBloqueoEsPorClaveIndependiente() {
        service.registrarFallo("admin");
        service.registrarFallo("admin");
        service.registrarFallo("admin");

        assertThat(service.estaBloqueado("admin")).isTrue();
        assertThat(service.estaBloqueado("otro-usuario")).isFalse();
    }
}
