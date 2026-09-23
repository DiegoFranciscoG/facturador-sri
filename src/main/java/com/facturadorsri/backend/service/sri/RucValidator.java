package com.facturadorsri.backend.service.sri;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validacion de RUC ecuatoriano (13 digitos). El tercer digito determina el
 * tipo de contribuyente y por lo tanto el algoritmo de verificacion:
 * 0-5 persona natural (cedula + establecimiento), 6 sociedad publica,
 * 9 sociedad privada o extranjero sin cedula.
 * Fuente: docs/investigacion.md seccion 5 (GitLab Mintel, entidad publica EC).
 */
@Component
@RequiredArgsConstructor
public class RucValidator {

    private static final int[] COEF_PUBLICO = {3, 2, 7, 6, 5, 4, 3, 2};
    private static final int[] COEF_PRIVADO = {4, 3, 2, 7, 6, 5, 4, 3, 2};

    private final CedulaValidator cedulaValidator;

    public boolean esValido(String ruc) {
        if (ruc == null || !ruc.matches("\\d{13}")) {
            return false;
        }
        int provincia = Integer.parseInt(ruc.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return false;
        }
        int tercerDigito = digito(ruc, 2);
        if (tercerDigito <= 5) {
            return esValidoPersonaNatural(ruc);
        }
        if (tercerDigito == 6) {
            return esValidoPublico(ruc);
        }
        if (tercerDigito == 9) {
            return esValidoPrivadoOExtranjero(ruc);
        }
        return false;
    }

    private boolean esValidoPersonaNatural(String ruc) {
        String cedula = ruc.substring(0, 10);
        String establecimiento = ruc.substring(10, 13);
        return cedulaValidator.esValida(cedula) && !establecimiento.equals("000");
    }

    private boolean esValidoPublico(String ruc) {
        Integer verificador = calcularVerificador(ruc, COEF_PUBLICO);
        if (verificador == null || verificador != digito(ruc, 8)) {
            return false;
        }
        String establecimiento = ruc.substring(9, 13);
        return !establecimiento.equals("0000");
    }

    private boolean esValidoPrivadoOExtranjero(String ruc) {
        Integer verificador = calcularVerificador(ruc, COEF_PRIVADO);
        if (verificador == null || verificador != digito(ruc, COEF_PRIVADO.length)) {
            return false;
        }
        String establecimiento = ruc.substring(10, 13);
        return !establecimiento.equals("000");
    }

    /** Modulo 11: suma = sum(digito[i] * coeficiente[i]); verificador = 11 - (suma % 11). */
    private Integer calcularVerificador(String ruc, int[] coeficientes) {
        int suma = 0;
        for (int i = 0; i < coeficientes.length; i++) {
            suma += digito(ruc, i) * coeficientes[i];
        }
        int resto = suma % 11;
        int verificador = resto == 0 ? 0 : 11 - resto;
        return verificador == 10 ? null : verificador;
    }

    private int digito(String texto, int posicion) {
        return Character.getNumericValue(texto.charAt(posicion));
    }
}
