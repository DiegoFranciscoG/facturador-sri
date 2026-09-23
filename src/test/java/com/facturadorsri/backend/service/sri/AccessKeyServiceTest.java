package com.facturadorsri.backend.service.sri;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessKeyServiceTest {

    private final AccessKeyService service = new AccessKeyService();

    @Test
    void calculaElDigitoVerificadorCalculadoAMano() {
        // Base de 48 digitos y digito verificador (6) calculados manualmente
        // en la revision de esta clase: fecha 22092026, tipo 01,
        // ruc 1790012344001, ambiente 1, estab 001, ptoEmi 001,
        // secuencial 000000001, codigo numerico 12345678, tipo emision 1.
        String base48 = "220920260117900123440011001001000000001123456781";
        assertThat(service.digitoVerificador(base48)).isEqualTo(6);
    }

    @Test
    void unaClaveDeAccesoValidaPasaLaValidacion() {
        String claveCompleta = "2209202601179001234400110010010000000011234567816";
        assertThat(service.esValida(claveCompleta)).isTrue();
    }

    @Test
    void unaClaveDeAccesoConUnDigitoAlteradoNoPasaLaValidacion() {
        // Se altera el ultimo digito del codigo numerico (posicion 47 de 49)
        String claveAlterada = "2209202601179001234400110010010000000011234567826";
        assertThat(service.esValida(claveAlterada)).isFalse();
    }

    @RepeatedTest(20)
    void unaClaveGeneradaSiempreEsInternamenteValida() {
        String clave = service.generar(
                LocalDate.of(2026, 9, 22), "01", "1790012344001", "1", "001", "001", "000000001");

        assertThat(clave).hasSize(49);
        assertThat(clave).matches("\\d{49}");
        assertThat(service.esValida(clave)).isTrue();
    }

    @Test
    void generarConRucInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> service.generar(
                LocalDate.of(2026, 9, 22), "01", "123", "1", "001", "001", "000000001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generarConAmbienteInvalidoLanzaExcepcion() {
        assertThatThrownBy(() -> service.generar(
                LocalDate.of(2026, 9, 22), "01", "1790012344001", "3", "001", "001", "000000001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void esValidaRechazaLongitudIncorrecta() {
        assertThat(service.esValida("12345")).isFalse();
        assertThat(service.esValida(null)).isFalse();
    }
}
