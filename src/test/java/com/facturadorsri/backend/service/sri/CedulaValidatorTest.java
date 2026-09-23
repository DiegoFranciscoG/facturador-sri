package com.facturadorsri.backend.service.sri;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CedulaValidatorTest {

    private final CedulaValidator validator = new CedulaValidator();

    @Test
    void aceptaCedulaFicticiaValidaCalculadaAMano() {
        // 172548963 + digito verificador 5, calculado con coeficientes 2,1,2,1,2,1,2,1,2
        assertThat(validator.esValida("1725489635")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1725489634", "1725489636", "0000000000", "999999999", "17254896350"})
    void rechazaCedulasInvalidas(String cedula) {
        assertThat(validator.esValida(cedula)).isFalse();
    }

    @Test
    void rechazaNull() {
        assertThat(validator.esValida(null)).isFalse();
    }

    @Test
    void rechazaProvinciaFueraDeRango() {
        // provincia 25 no existe (maximo 24)
        assertThat(validator.esValida("2525489635")).isFalse();
    }

    @Test
    void rechazaTercerDigitoMayorA5() {
        // tercer digito 9 no es valido para cedula (solo para RUC de sociedades)
        assertThat(validator.esValida("1795489635")).isFalse();
    }
}
