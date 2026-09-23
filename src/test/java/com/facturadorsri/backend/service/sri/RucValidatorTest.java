package com.facturadorsri.backend.service.sri;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RucValidatorTest {

    private final RucValidator validator = new RucValidator(new CedulaValidator());

    @Test
    void aceptaRucPersonaNaturalValido() {
        // cedula 1725489635 (valida, ver CedulaValidatorTest) + establecimiento 001
        assertThat(validator.esValido("1725489635001")).isTrue();
    }

    @Test
    void rechazaRucPersonaNaturalConEstablecimientoCero() {
        assertThat(validator.esValido("1725489635000")).isFalse();
    }

    @Test
    void rechazaRucPersonaNaturalConCedulaInvalida() {
        assertThat(validator.esValido("1725489634001")).isFalse();
    }

    @Test
    void aceptaRucSociedadPrivadaValido() {
        // calculado a mano en docs/investigacion.md seccion 5: base 179001234,
        // coeficientes 4,3,2,7,6,5,4,3,2 -> verificador 4 -> 1790012344 + 001
        assertThat(validator.esValido("1790012344001")).isTrue();
    }

    @Test
    void rechazaRucSociedadPrivadaConDigitoVerificadorAlterado() {
        assertThat(validator.esValido("1790012345001")).isFalse();
    }

    @Test
    void rechazaRucConLongitudIncorrecta() {
        assertThat(validator.esValido("179001234400")).isFalse();
    }

    @Test
    void rechazaRucConProvinciaInvalida() {
        assertThat(validator.esValido("9990012344001")).isFalse();
    }

    @Test
    void rechazaNull() {
        assertThat(validator.esValido(null)).isFalse();
    }
}
