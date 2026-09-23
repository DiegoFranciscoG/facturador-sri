package com.facturadorsri.backend.service.sri;

import org.springframework.stereotype.Component;

/**
 * Validacion de cedula ecuatoriana: 10 digitos, algoritmo modulo 10.
 * Fuente: docs/investigacion.md seccion 5 (GitLab Mintel, entidad publica EC).
 */
@Component
public class CedulaValidator {

    private static final int[] COEFICIENTES = {2, 1, 2, 1, 2, 1, 2, 1, 2};

    public boolean esValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return false;
        }
        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito > 5) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int producto = Character.getNumericValue(cedula.charAt(i)) * COEFICIENTES[i];
            if (producto > 9) {
                producto -= 9;
            }
            suma += producto;
        }
        int digitoVerificador = (10 - (suma % 10)) % 10;
        return digitoVerificador == Character.getNumericValue(cedula.charAt(9));
    }
}
