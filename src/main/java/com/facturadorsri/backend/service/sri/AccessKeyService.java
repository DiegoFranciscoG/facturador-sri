package com.facturadorsri.backend.service.sri;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Clave de acceso de 49 digitos del esquema offline del SRI.
 * Estructura y algoritmo verificados en docs/investigacion.md seccion 3.
 */
@Component
public class AccessKeyService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final SecureRandom ALEATORIO = new SecureRandom();

    public String generar(LocalDate fechaEmision, String tipoComprobante, String ruc, String ambiente,
                           String establecimiento, String puntoEmision, String secuencial) {
        if (ruc == null || !ruc.matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC del emisor debe tener 13 digitos");
        }
        if (tipoComprobante == null || !tipoComprobante.matches("\\d{2}")) {
            throw new IllegalArgumentException("El tipo de comprobante debe tener 2 digitos");
        }
        if (ambiente == null || !ambiente.matches("[12]")) {
            throw new IllegalArgumentException("El ambiente debe ser 1 (pruebas) o 2 (produccion)");
        }
        if (establecimiento == null || !establecimiento.matches("\\d{3}")) {
            throw new IllegalArgumentException("El establecimiento debe tener 3 digitos");
        }
        if (puntoEmision == null || !puntoEmision.matches("\\d{3}")) {
            throw new IllegalArgumentException("El punto de emision debe tener 3 digitos");
        }
        if (secuencial == null || !secuencial.matches("\\d{9}")) {
            throw new IllegalArgumentException("El secuencial debe tener 9 digitos");
        }

        StringBuilder clave = new StringBuilder(48);
        clave.append(fechaEmision.format(FORMATO_FECHA));
        clave.append(tipoComprobante);
        clave.append(ruc);
        clave.append(ambiente);
        clave.append(establecimiento);
        clave.append(puntoEmision);
        clave.append(secuencial);
        clave.append(codigoNumericoAleatorio());
        clave.append("1"); // tipo de emision: siempre normal en esquema offline

        String base48 = clave.toString();
        if (base48.length() != 48) {
            throw new IllegalStateException("La clave de acceso base debe tener 48 digitos, tiene " + base48.length());
        }
        return base48 + digitoVerificador(base48);
    }

    private String codigoNumericoAleatorio() {
        int numero = 10_000_000 + ALEATORIO.nextInt(90_000_000);
        return Integer.toString(numero);
    }

    /**
     * Modulo 11 sobre los 48 digitos: coeficientes ciclicos 7,6,5,4,3,2 de
     * izquierda a derecha. resultado = 11 - (suma % 11); 10 -> 1; 11 -> 0.
     */
    public int digitoVerificador(String base48digitos) {
        if (base48digitos == null || !base48digitos.matches("\\d{48}")) {
            throw new IllegalArgumentException("Se requieren exactamente 48 digitos para calcular el verificador");
        }
        int suma = 0;
        int multiplicador = 7;
        for (int i = 0; i < base48digitos.length(); i++) {
            int digito = Character.getNumericValue(base48digitos.charAt(i));
            suma += digito * multiplicador;
            multiplicador = multiplicador > 2 ? multiplicador - 1 : 7;
        }
        int resultado = 11 - (suma % 11);
        if (resultado == 10) {
            return 1;
        }
        if (resultado == 11) {
            return 0;
        }
        return resultado;
    }

    public boolean esValida(String claveAcceso) {
        if (claveAcceso == null || !claveAcceso.matches("\\d{49}")) {
            return false;
        }
        String base48 = claveAcceso.substring(0, 48);
        int esperado = Character.getNumericValue(claveAcceso.charAt(48));
        return digitoVerificador(base48) == esperado;
    }
}
